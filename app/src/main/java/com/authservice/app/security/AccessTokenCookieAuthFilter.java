package com.authservice.app.security;

import com.auth.api.model.Principal;
import com.auth.spi.TokenService;
import com.authservice.app.domain.auth.sso.config.SsoProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 브라우저가 `ACCESS_TOKEN` 쿠키만 보내는 경우를 위한 보조 인증 필터입니다.
 * Authorization 헤더가 없을 때만 쿠키를 읽어 SecurityContext를 채웁니다.
 */
public class AccessTokenCookieAuthFilter extends OncePerRequestFilter {

	private final TokenService tokenService;
	private final String accessTokenCookieName;
	private final String sessionCookieName;

	public AccessTokenCookieAuthFilter(TokenService tokenService, String accessTokenCookieName, SsoProperties ssoProperties) {
		this.tokenService = tokenService;
		this.accessTokenCookieName = accessTokenCookieName;
		this.sessionCookieName = ssoProperties.getSession().getCookieName();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		try {
			if (SecurityContextHolder.getContext().getAuthentication() == null
				&& !hasAuthorizationHeader(request)
				&& !hasSessionCookie(request)) {
				extractAccessToken(request)
					.map(tokenService::verifyAccessToken)
					.ifPresent(this::setAuthentication);
			}
			filterChain.doFilter(request, response);
		} catch (Exception ex) {
			SecurityContextHolder.clearContext();
			filterChain.doFilter(request, response);
		}
	}

	private boolean hasAuthorizationHeader(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		return authorization != null && !authorization.isBlank();
	}

	private boolean hasSessionCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return false;
		}
		return Arrays.stream(cookies)
			.anyMatch(cookie -> sessionCookieName.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank());
	}

	private java.util.Optional<String> extractAccessToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return java.util.Optional.empty();
		}
		return Arrays.stream(cookies)
			.filter(cookie -> accessTokenCookieName.equals(cookie.getName()))
			.map(Cookie::getValue)
			.filter(value -> value != null && !value.isBlank())
			.findFirst();
	}

	private void setAuthentication(Principal principal) {
		List<GrantedAuthority> authorities = principal.getAuthorities().stream()
			.filter(value -> value != null && !value.isBlank())
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toList());
		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(principal.getUserId(), null, authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
