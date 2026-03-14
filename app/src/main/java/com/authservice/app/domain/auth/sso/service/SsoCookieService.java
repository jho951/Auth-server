package com.authservice.app.domain.auth.sso.service;

import com.authservice.app.domain.auth.sso.config.SsoProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SsoCookieService {

	private final SsoProperties properties;

	public SsoCookieService(SsoProperties properties) {
		this.properties = properties;
	}

	public Optional<String> extractSessionId(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		return Arrays.stream(cookies)
			.filter(cookie -> properties.getSession().getCookieName().equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst();
	}

	public ResponseEntity<Void> writeSessionCookie(String sessionId) {
		ResponseCookie cookie = ResponseCookie.from(properties.getSession().getCookieName(), sessionId)
			.httpOnly(properties.getSession().isCookieHttpOnly())
			.secure(properties.getSession().isCookieSecure())
			.path(properties.getSession().getCookiePath())
			.sameSite(properties.getSession().getCookieSameSite())
			.maxAge(properties.getSession().getTtlSeconds())
			.build();

		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.build();
	}

	public ResponseEntity<Void> clearSessionCookie() {
		ResponseCookie cookie = ResponseCookie.from(properties.getSession().getCookieName(), "")
			.httpOnly(properties.getSession().isCookieHttpOnly())
			.secure(properties.getSession().isCookieSecure())
			.path(properties.getSession().getCookiePath())
			.sameSite(properties.getSession().getCookieSameSite())
			.maxAge(0)
			.build();

		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.build();
	}
}
