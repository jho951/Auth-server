package com.authservice.app.security;

import com.authservice.app.domain.auth.model.AuthPrincipal;
import com.authservice.app.domain.auth.sso.model.SsoStorePayloads.SsoSessionPayload;
import com.authservice.app.domain.auth.sso.service.SsoSessionStore;
import io.github.jho951.platform.security.api.SecurityContext;
import io.github.jho951.platform.security.api.SecurityContextResolver;
import io.github.jho951.platform.security.web.PlatformSecurityServletFilter;
import io.github.jho951.platform.security.web.SecurityAuditPublisher;
import io.github.jho951.platform.security.web.SecurityDownstreamIdentityPropagator;
import io.github.jho951.platform.security.web.SecurityIngressAdapter;
import io.github.jho951.platform.security.web.SecurityIngressRequestFactory;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PlatformSecurityAuthConfig {

	private static final String ACCESS_TOKEN_ATTRIBUTE = "auth.accessToken";
	private static final String SESSION_ID_ATTRIBUTE = "auth.sessionId";

	@Bean
	@Primary
	public SecurityContextResolver securityContextResolver(
		AuthJwtTokenService tokenService,
		SsoSessionStore ssoSessionStore
	) {
		return request -> {
			Map<String, String> attributes = new LinkedHashMap<>(request.attributes());

			String accessToken = trimToNull(attributes.remove(ACCESS_TOKEN_ATTRIBUTE));
			if (accessToken != null) {
				return fromPrincipal(tokenService.verifyAccessToken(accessToken), attributes);
			}

			String sessionId = trimToNull(attributes.remove(SESSION_ID_ATTRIBUTE));
			if (sessionId != null) {
				return ssoSessionStore.findSession(sessionId)
					.map(payload -> fromSession(payload, attributes))
					.orElseGet(() -> anonymous(attributes));
			}

			return anonymous(attributes);
		};
	}

	@Bean
	public PlatformSecurityServletFilter platformSecurityServletFilter(
		SecurityIngressAdapter securityIngressAdapter,
		SecurityContextResolver securityContextResolver,
		SecurityIngressRequestFactory securityIngressRequestFactory,
		SecurityDownstreamIdentityPropagator downstreamIdentityPropagator,
		SecurityAuditPublisher securityAuditPublisher
	) {
		return new PlatformSecurityServletFilter(
			securityIngressAdapter,
			securityContextResolver,
			Clock.systemUTC(),
			securityIngressRequestFactory,
			downstreamIdentityPropagator,
			securityAuditPublisher
		);
	}

	private static SecurityContext fromPrincipal(AuthPrincipal principal, Map<String, String> attributes) {
		Map<String, String> merged = new LinkedHashMap<>(attributes);
		principal.attributes().forEach((key, value) -> {
			if (key != null && value != null) {
				merged.putIfAbsent(key, String.valueOf(value));
			}
		});
		return new SecurityContext(
			true,
			principal.userId(),
			toRoleSet(principal.roles()),
			merged
		);
	}

	private static SecurityContext fromSession(SsoSessionPayload payload, Map<String, String> attributes) {
		Map<String, String> merged = new LinkedHashMap<>(attributes);
		putIfPresent(merged, "email", payload.getEmail());
		putIfPresent(merged, "name", payload.getName());
		putIfPresent(merged, "avatarUrl", payload.getAvatarUrl());
		putIfPresent(merged, "status", payload.getStatus());
		return new SecurityContext(
			true,
			payload.getUserId(),
			toRoleSet(payload.getRoles()),
			merged
		);
	}

	private static SecurityContext anonymous(Map<String, String> attributes) {
		return new SecurityContext(false, null, Set.of(), attributes);
	}

	private static Set<String> toRoleSet(java.util.List<String> roles) {
		if (roles == null) {
			return Set.of();
		}
		return roles.stream()
			.map(PlatformSecurityAuthConfig::trimToNull)
			.filter(value -> value != null)
			.collect(Collectors.toUnmodifiableSet());
	}

	private static void putIfPresent(Map<String, String> values, String key, String value) {
		if (value != null && !value.isBlank()) {
			values.putIfAbsent(key, value);
		}
	}

	private static String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
