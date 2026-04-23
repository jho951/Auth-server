package com.authservice.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.authservice.app.domain.auth.sso.config.SsoProperties;
import com.authservice.app.domain.auth.sso.service.SsoOAuth2FailureHandler;
import com.authservice.app.domain.auth.sso.service.SsoOAuth2SuccessHandler;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

class SecurityRouteClassificationTest {

	private SecurityConfig securityConfig;

	@BeforeEach
	void setUp() {
		securityConfig = new SecurityConfig(
			new RestAuthHandlers.EntryPoint(),
			new RestAuthHandlers.Denied(),
			new SsoProperties(),
			org.mockito.Mockito.mock(SsoOAuth2SuccessHandler.class),
			org.mockito.Mockito.mock(SsoOAuth2FailureHandler.class),
			org.mockito.Mockito.mock(Filter.class),
			org.mockito.Mockito.mock(PlatformSecurityRequestAttributeBridgeFilter.class),
			org.mockito.Mockito.mock(CookieCsrfOriginGuardFilter.class),
			org.mockito.Mockito.mock(Environment.class)
		);
	}

	@Test
	void classifiesPublicRoutes() {
		String[] publicRoutes = ReflectionTestUtils.invokeMethod(securityConfig, "publicRequestMatchers");

		assertThat(publicRoutes)
			.contains(
				"/auth/login",
				"/auth/refresh",
				"/auth/logout",
				"/auth/me",
				"/auth/session",
				"/auth/internal/session/validate"
			)
			.doesNotContain("/internal/auth/**");
	}

	@Test
	void classifiesProtectedRoutes() {
		String[] protectedRoutes = ReflectionTestUtils.invokeMethod(securityConfig, "protectedRequestMatchers");

		assertThat(protectedRoutes).containsExactly("/api/**");
	}

	@Test
	void classifiesInternalRoutes() {
		String[] internalRoutes = ReflectionTestUtils.invokeMethod(securityConfig, "internalPassThroughRequestMatchers");

		assertThat(internalRoutes).containsExactly("/auth/internal/**", "/internal/**");
	}
}
