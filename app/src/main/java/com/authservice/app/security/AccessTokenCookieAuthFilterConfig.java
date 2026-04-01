package com.authservice.app.security;

import com.auth.spi.TokenService;
import com.authservice.app.domain.auth.sso.config.SsoProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessTokenCookieAuthFilterConfig {

	@Bean
	public AccessTokenCookieAuthFilter accessTokenCookieAuthFilter(
		TokenService tokenService,
		SsoProperties ssoProperties,
		@Value("${AUTH_ACCESS_COOKIE_NAME:ACCESS_TOKEN}") String accessTokenCookieName
	) {
		return new AccessTokenCookieAuthFilter(tokenService, accessTokenCookieName, ssoProperties);
	}
}
