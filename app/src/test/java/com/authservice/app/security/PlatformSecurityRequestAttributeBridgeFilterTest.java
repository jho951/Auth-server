package com.authservice.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.authservice.app.domain.auth.sso.config.SsoProperties;
import com.authservice.app.domain.auth.userdirectory.config.InternalApiProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class PlatformSecurityRequestAttributeBridgeFilterTest {

	@Test
	void bridgesSessionAndAccessTokenCookies() throws ServletException, IOException {
		PlatformSecurityRequestAttributeBridgeFilter filter = newFilter();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/session");
		request.setCookies(
			new jakarta.servlet.http.Cookie("sso_session", "session-123"),
			new jakarta.servlet.http.Cookie("ACCESS_TOKEN", "access-token-123")
		);

		run(filter, request);

		assertThat(request.getAttribute("auth.sessionId")).isEqualTo("session-123");
		assertThat(request.getAttribute("auth.accessToken")).isEqualTo("access-token-123");
	}

	@Test
	void bridgesInternalCallerProofForInternalRoute() throws ServletException, IOException {
		PlatformSecurityRequestAttributeBridgeFilter filter = newFilter();
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/internal/session/validate");
		request.addHeader(InternalApiProperties.INTERNAL_SECRET_HEADER, "local-internal-api-key");

		run(filter, request);

		assertThat(request.getAttribute("auth.authenticated")).isEqualTo("true");
		assertThat(request.getAttribute("auth.principal")).isEqualTo("internal-service");
		assertThat(request.getAttribute("auth.roles")).isEqualTo("ROLE_INTERNAL");
	}

	private static PlatformSecurityRequestAttributeBridgeFilter newFilter() {
		SsoProperties ssoProperties = new SsoProperties();
		InternalApiProperties internalApiProperties = new InternalApiProperties();
		internalApiProperties.setKey("local-internal-api-key");
		return new PlatformSecurityRequestAttributeBridgeFilter(
			ssoProperties,
			internalApiProperties,
			"ACCESS_TOKEN"
		);
	}

	private static void run(
		PlatformSecurityRequestAttributeBridgeFilter filter,
		MockHttpServletRequest request
	) throws ServletException, IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = new MockFilterChain();
		filter.doFilter(request, response, chain);
	}
}
