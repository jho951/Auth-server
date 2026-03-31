package com.authservice.app.security;

import com.auth.config.jwt.AuthJwtProperties;
import com.auth.spi.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AuthJwtTokenServiceConfig {

	@Bean
	@Primary
	public TokenService tokenService(
		AuthJwtProperties props,
		@Value("${AUTH_JWT_AUDIENCE:block-service}") String audience
	) {
		return new AuthJwtTokenService(
			props.getSecret(),
			audience,
			props.getAccessSeconds(),
			props.getRefreshSeconds()
		);
	}
}
