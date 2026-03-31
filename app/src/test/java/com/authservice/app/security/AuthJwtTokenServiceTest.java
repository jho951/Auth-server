package com.authservice.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.api.model.Principal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuthJwtTokenServiceTest {

	private static final String SECRET = "abcdefghijklmnopqrstuvwxyz12345678901234567890";
	private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

	@Test
	void accessTokenContainsIssuerAuthService() {
		AuthJwtTokenService tokenService = new AuthJwtTokenService(SECRET, "block-service", 60, 120);
		Principal principal = new Principal("46c45ce7-d50c-436e-9394-263941839cf7", List.of("USER"));

		String token = tokenService.issueAccessToken(principal);

		Claims claims = Jwts.parserBuilder()
			.setSigningKey(KEY)
			.build()
			.parseClaimsJws(token)
			.getBody();

		assertThat(claims.getIssuer()).isEqualTo("auth-service");
		assertThat(claims.getAudience()).isEqualTo("block-service");
		assertThat(claims.getSubject()).isEqualTo(principal.getUserId());
		assertThat(claims.get("token_type", String.class)).isEqualTo("access");
		assertThat(claims.get("authorities")).isInstanceOf(List.class);
		assertThat(claims.get("roles")).isInstanceOf(List.class);
	}

	@Test
	void refreshTokenDoesNotNeedIssuerButStillVerifies() {
		AuthJwtTokenService tokenService = new AuthJwtTokenService(SECRET, "block-service", 60, 120);
		Principal principal = new Principal("46c45ce7-d50c-436e-9394-263941839cf7", List.of("USER"));

		String token = tokenService.issueRefreshToken(principal);

		Claims claims = Jwts.parserBuilder()
			.setSigningKey(KEY)
			.build()
			.parseClaimsJws(token)
			.getBody();

		assertThat(claims.getIssuer()).isNull();
		assertThat(claims.getAudience()).isEqualTo("block-service");
		assertThat(claims.get("token_type", String.class)).isEqualTo("refresh");
		assertThat(tokenService.verifyRefreshToken(token).getUserId()).isEqualTo(principal.getUserId());
	}
}
