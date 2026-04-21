package com.authservice.app.security;

import com.authservice.app.domain.auth.sso.config.SsoProperties;
import com.authservice.app.domain.auth.userdirectory.config.InternalApiProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PlatformSecurityRequestAttributeBridgeFilter extends OncePerRequestFilter {

	private static final String ACCESS_TOKEN_ATTRIBUTE = "auth.accessToken";
	private static final String SESSION_ID_ATTRIBUTE = "auth.sessionId";
	private static final String AUTHENTICATED_ATTRIBUTE = "auth.authenticated";
	private static final String PRINCIPAL_ATTRIBUTE = "auth.principal";
	private static final String ROLES_ATTRIBUTE = "auth.roles";
	private static final String INTERNAL_PRINCIPAL = "internal-service";
	private static final String INTERNAL_ROLE = "ROLE_INTERNAL";

	private final SsoProperties ssoProperties;
	private final InternalApiProperties internalApiProperties;
	private final String accessTokenCookieName;

	public PlatformSecurityRequestAttributeBridgeFilter(
		SsoProperties ssoProperties,
		InternalApiProperties internalApiProperties,
		@Value("${AUTH_ACCESS_COOKIE_NAME:ACCESS_TOKEN}") String accessTokenCookieName
	) {
		this.ssoProperties = ssoProperties;
		this.internalApiProperties = internalApiProperties;
		this.accessTokenCookieName = accessTokenCookieName;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		bridgeCookieCredentials(request);
		bridgeInternalCallerProof(request);
		filterChain.doFilter(request, response);
	}

	private void bridgeCookieCredentials(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return;
		}

		String sessionCookieName = ssoProperties.getSession().getCookieName();
		for (Cookie cookie : cookies) {
			if (cookie == null) {
				continue;
			}
			String name = cookie.getName();
			String value = cookie.getValue();
			if (value == null || value.isBlank()) {
				continue;
			}
			if (sessionCookieName.equals(name)) {
				request.setAttribute(SESSION_ID_ATTRIBUTE, value);
				continue;
			}
			if (accessTokenCookieName.equals(name)) {
				request.setAttribute(ACCESS_TOKEN_ATTRIBUTE, value);
			}
		}
	}

	private void bridgeInternalCallerProof(HttpServletRequest request) {
		if (!isInternalPath(request)) {
			return;
		}

		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		String internalSecret = request.getHeader(InternalApiProperties.INTERNAL_SECRET_HEADER);
		try {
			internalApiProperties.validateInternalAccess(authorization, internalSecret);
		} catch (RuntimeException ex) {
			return;
		}

		request.setAttribute(AUTHENTICATED_ATTRIBUTE, Boolean.TRUE.toString());
		request.setAttribute(PRINCIPAL_ATTRIBUTE, INTERNAL_PRINCIPAL);
		request.setAttribute(ROLES_ATTRIBUTE, INTERNAL_ROLE);
	}

	private boolean isInternalPath(HttpServletRequest request) {
		String path = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
		}
		return path.equals("/internal")
			|| path.startsWith("/internal/")
			|| path.equals("/auth/internal")
			|| path.startsWith("/auth/internal/");
	}
}
