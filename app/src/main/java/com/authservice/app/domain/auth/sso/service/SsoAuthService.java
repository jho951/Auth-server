package com.authservice.app.domain.auth.sso.service;

import com.authservice.app.domain.auth.sso.config.SsoProperties;
import com.authservice.app.domain.auth.sso.model.GithubUserProfile;
import com.authservice.app.domain.auth.sso.model.SsoPrincipal;
import com.authservice.app.domain.auth.sso.model.SsoStorePayloads.SsoSessionPayload;
import com.authservice.app.domain.auth.sso.model.SsoStorePayloads.SsoStatePayload;
import com.authservice.app.domain.auth.sso.model.SsoStorePayloads.SsoTicketPayload;
import com.authservice.app.common.base.constant.ErrorCode;
import com.authservice.app.common.base.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SsoAuthService {

	private static final Logger log = LoggerFactory.getLogger(SsoAuthService.class);

	private final SsoProperties properties;
	private final SsoSessionStore sessionStore;
	private final GithubOAuthClient githubOAuthClient;
	private final SsoUserService ssoUserService;
	private final SsoCookieService cookieService;

	public SsoAuthService(
		SsoProperties properties,
		SsoSessionStore sessionStore,
		GithubOAuthClient githubOAuthClient,
		SsoUserService ssoUserService,
		SsoCookieService cookieService
	) {
		this.properties = properties;
		this.sessionStore = sessionStore;
		this.githubOAuthClient = githubOAuthClient;
		this.ssoUserService = ssoUserService;
		this.cookieService = cookieService;
	}

	public String buildGithubAuthorizeUrl(String redirectUri) {
		validateRedirectUri(redirectUri);

		String state = UUID.randomUUID().toString();
		Instant expiresAt = Instant.now().plusSeconds(properties.getStateTtlSeconds());
		sessionStore.saveState(state, new SsoStatePayload(redirectUri, expiresAt), expiresAt);

		return UriComponentsBuilder.fromUriString(properties.getGithub().getAuthorizeUri())
			.queryParam("client_id", properties.getGithub().getClientId())
			.queryParam("redirect_uri", properties.getGithub().getCallbackUri())
			.queryParam("scope", String.join(" ", properties.getGithub().getScopes()))
			.queryParam("state", state)
			.build()
			.encode()
			.toUriString();
	}

	public URI handleGithubCallback(String code, String state) {
		SsoStatePayload statePayload = sessionStore.consumeState(state)
			.orElseThrow(() -> {
				log.warn("SSO callback rejected: state not found or expired. state={}", state);
				return new GlobalException(ErrorCode.INVALID_REQUEST);
			});

		GithubUserProfile githubUser = githubOAuthClient.fetchUserProfile(code);
		SsoPrincipal principal = ssoUserService.verifyGithubUser(githubUser);

		String ticket = UUID.randomUUID().toString();
		Instant expiresAt = Instant.now().plusSeconds(properties.getTicketTtlSeconds());
		sessionStore.saveTicket(
			ticket,
			new SsoTicketPayload(
				principal.getUserId(),
				principal.getEmail(),
				principal.getName(),
				principal.getAvatarUrl(),
				principal.getRoles(),
				expiresAt
			),
			expiresAt
		);

		return URI.create(UriComponentsBuilder.fromUriString(statePayload.getRedirectUri())
			.queryParam("ticket", ticket)
			.build(true)
			.toUriString());
	}

	public org.springframework.http.ResponseEntity<Void> exchangeTicket(String ticket) {
		SsoTicketPayload payload = sessionStore.consumeTicket(ticket)
			.orElseThrow(() -> new GlobalException(ErrorCode.UNAUTHORIZED));

		String sessionId = UUID.randomUUID().toString();
		Instant expiresAt = Instant.now().plusSeconds(properties.getSession().getTtlSeconds());
		sessionStore.saveSession(
			sessionId,
			new SsoSessionPayload(
				payload.getUserId(),
				payload.getEmail(),
				payload.getName(),
				payload.getAvatarUrl(),
				payload.getRoles(),
				expiresAt
			),
			expiresAt
		);

		return cookieService.writeSessionCookie(sessionId);
	}

	public SsoPrincipal getCurrentUser(HttpServletRequest request) {
		String sessionId = cookieService.extractSessionId(request)
			.orElseThrow(() -> new GlobalException(ErrorCode.NEED_LOGIN));

		SsoSessionPayload payload = sessionStore.findSession(sessionId)
			.orElseThrow(() -> new GlobalException(ErrorCode.NEED_LOGIN));

		return new SsoPrincipal(
			payload.getUserId(),
			payload.getEmail(),
			payload.getName(),
			payload.getAvatarUrl(),
			payload.getRoles() == null ? List.of() : payload.getRoles()
		);
	}

	public org.springframework.http.ResponseEntity<Void> logout(HttpServletRequest request) {
		cookieService.extractSessionId(request).ifPresent(sessionStore::revokeSession);
		return cookieService.clearSessionCookie();
	}

	private void validateRedirectUri(String redirectUri) {
		String normalized = normalizeRedirectUri(redirectUri);

		boolean allowed = properties.getFrontend().getAllowedRedirectUris().stream()
			.map(this::normalizeRedirectUri)
			.anyMatch(normalized::equals);

		if (!allowed) {
			throw new GlobalException(ErrorCode.INVALID_REQUEST);
		}
	}

	private String normalizeRedirectUri(String redirectUri) {
		URI requested = URI.create(redirectUri);
		String path = requested.getPath();
		if (path == null || path.isBlank()) {
			path = "/";
		}

		return UriComponentsBuilder.newInstance()
			.scheme(requested.getScheme())
			.host(requested.getHost())
			.port(requested.getPort())
			.path(path)
			.build()
			.toUriString();
	}
}
