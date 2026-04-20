package com.authservice.app.security;

import com.authservice.app.domain.auth.model.AuthPrincipal;
import com.authservice.app.domain.auth.sso.model.SsoStorePayloads.SsoSessionPayload;
import com.authservice.app.domain.auth.sso.service.SsoSessionStore;
import io.github.jho951.platform.security.api.SecurityContext;
import io.github.jho951.platform.security.api.SecurityContextResolver;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PlatformSecurityAuthConfig {

	private static final String ACCESS_TOKEN_ATTRIBUTE = "auth.accessToken";
	private static final String SESSION_ID_ATTRIBUTE = "auth.sessionId";
	private static final String AUTHENTICATED_ATTRIBUTE = "auth.authenticated";
	private static final String PRINCIPAL_ATTRIBUTE = "auth.principal";
	private static final String ROLES_ATTRIBUTE = "auth.roles";

	@Bean
	@Primary
	public SecurityContextResolver securityContextResolver(
		AuthJwtTokenService tokenService,
		SsoSessionStore sessionStore
	) {
		return request -> resolveAccessToken(tokenService, request.attributes())
			.or(() -> resolveSession(sessionStore, request.attributes()))
			.orElseGet(() -> requestPrincipalContext(request.attributes()));
	}

	private static Optional<SecurityContext> resolveAccessToken(
		AuthJwtTokenService tokenService,
		Map<String, String> attributes
	) {
		String accessToken = stringAttribute(attributes, ACCESS_TOKEN_ATTRIBUTE);
		if (accessToken == null) {
			return Optional.empty();
		}

		try {
			return Optional.of(toSecurityContext(tokenService.verifyAccessToken(accessToken)));
		} catch (RuntimeException ex) {
			return Optional.empty();
		}
	}

	private static Optional<SecurityContext> resolveSession(
		SsoSessionStore sessionStore,
		Map<String, String> attributes
	) {
		String sessionId = stringAttribute(attributes, SESSION_ID_ATTRIBUTE);
		if (sessionId == null) {
			return Optional.empty();
		}
		return sessionStore.findSession(sessionId).map(PlatformSecurityAuthConfig::toSecurityContext);
	}

	private static SecurityContext requestPrincipalContext(Map<String, String> attributes) {
		if (!Boolean.parseBoolean(attributes.getOrDefault(AUTHENTICATED_ATTRIBUTE, "false"))) {
			return anonymousContext();
		}

		String principal = stringAttribute(attributes, PRINCIPAL_ATTRIBUTE);
		if (principal == null) {
			return anonymousContext();
		}
		return new SecurityContext(true, principal, parseRoles(attributes.get(ROLES_ATTRIBUTE)), Map.of());
	}

	private static SecurityContext toSecurityContext(AuthPrincipal principal) {
		Map<String, String> attributes = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : principal.attributes().entrySet()) {
			if (entry.getValue() instanceof String value && !value.isBlank()) {
				attributes.put(entry.getKey(), value);
			}
		}
		return new SecurityContext(true, principal.userId(), new LinkedHashSet<>(principal.roles()), attributes);
	}

	private static SecurityContext toSecurityContext(SsoSessionPayload payload) {
		Map<String, String> attributes = new LinkedHashMap<>();
		putIfPresent(attributes, "email", payload.getEmail());
		putIfPresent(attributes, "name", payload.getName());
		putIfPresent(attributes, "avatarUrl", payload.getAvatarUrl());
		putIfPresent(attributes, "status", payload.getStatus());
		return new SecurityContext(true, payload.getUserId(), rolesOrEmpty(payload.getRoles()), attributes);
	}

	private static Set<String> parseRoles(String rawRoles) {
		if (rawRoles == null || rawRoles.isBlank()) {
			return Set.of();
		}
		Set<String> roles = new LinkedHashSet<>();
		for (String role : rawRoles.split(",")) {
			String trimmed = role.trim();
			if (!trimmed.isBlank()) {
				roles.add(trimmed);
			}
		}
		return roles;
	}

	private static Set<String> rolesOrEmpty(Iterable<String> rawRoles) {
		if (rawRoles == null) {
			return Set.of();
		}
		Set<String> roles = new LinkedHashSet<>();
		for (String role : rawRoles) {
			if (role != null && !role.isBlank()) {
				roles.add(role);
			}
		}
		return roles;
	}

	private static SecurityContext anonymousContext() {
		return new SecurityContext(false, null, Set.of(), Map.of());
	}

	private static String stringAttribute(Map<String, String> attributes, String key) {
		String value = attributes.get(key);
		return value == null || value.isBlank() ? null : value;
	}

	private static void putIfPresent(Map<String, String> values, String key, String value) {
		if (value != null && !value.isBlank()) {
			values.putIfAbsent(key, value);
		}
	}
}
