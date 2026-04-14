package com.authservice.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AuthJwtTokenServiceConfig {

	@Bean
	@Primary
	public AuthJwtTokenService tokenService(
		@Value("${AUTH_JWT_SECRET:local-dev-auth-secret-local-dev-auth-secret}")
		String secret,
		@Value("${AUTH_ACCESS_EXPIRATION:1200}")
		long accessSeconds,
		@Value("${AUTH_REFRESH_EXPIRATION:30000}")
		long refreshSeconds,
		@Value("${AUTH_JWT_AUDIENCE:block-service}")
		String audience
	) {
		return new AuthJwtTokenService(
			secret,
			audience,
			accessSeconds,
			refreshSeconds
		);
	}
}
