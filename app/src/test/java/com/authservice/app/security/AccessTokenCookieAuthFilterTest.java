package com.authservice.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.auth.api.model.Principal;
import com.auth.spi.TokenService;
import com.authservice.app.domain.auth.sso.config.SsoProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class AccessTokenCookieAuthFilterTest {

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void readsAccessTokenFromCookieWhenAuthorizationHeaderIsMissing() throws Exception {
		TokenService tokenService = mock(TokenService.class);
		when(tokenService.verifyAccessToken("access-token")).thenReturn(
			new Principal("user-123", List.of("USER"))
		);

		AccessTokenCookieAuthFilter filter = new AccessTokenCookieAuthFilter(
			tokenService,
			"ACCESS_TOKEN",
			new SsoProperties()
		);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCookies(new Cookie("ACCESS_TOKEN", "access-token"));
		HttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = (req, res) -> { };

		filter.doFilter(request, response, chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user-123");
	}

	@Test
	void skipsCookieAuthWhenAuthorizationHeaderExists() throws Exception {
		TokenService tokenService = mock(TokenService.class);
		AccessTokenCookieAuthFilter filter = new AccessTokenCookieAuthFilter(
			tokenService,
			"ACCESS_TOKEN",
			new SsoProperties()
		);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer access-token");
		request.setCookies(new Cookie("ACCESS_TOKEN", "access-token"));
		HttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = (req, res) -> { };

		filter.doFilter(request, response, chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	void skipsCookieAuthWhenSessionCookieExists() throws Exception {
		TokenService tokenService = mock(TokenService.class);
		AccessTokenCookieAuthFilter filter = new AccessTokenCookieAuthFilter(
			tokenService,
			"ACCESS_TOKEN",
			new SsoProperties()
		);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCookies(
			new Cookie("sso_session", "session-id"),
			new Cookie("ACCESS_TOKEN", "access-token")
		);
		HttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = (req, res) -> { };

		filter.doFilter(request, response, chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}
}
