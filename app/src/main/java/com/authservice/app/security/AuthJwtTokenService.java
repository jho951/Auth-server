package com.authservice.app.security;

import com.auth.api.exception.AuthException;
import com.auth.api.exception.AuthFailureReason;
import com.auth.api.model.Principal;
import com.auth.common.utils.Strings;
import com.auth.spi.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * auth-service의 JWT 발급 구현체입니다.
 * access token에는 issuer를 명시적으로 넣어 downstream이 발급 주체를 판별할 수 있게 합니다.
 */
public class AuthJwtTokenService implements TokenService {

	private static final String KEY_ISSUER = "iss";
	private static final String KEY_AUDIENCE = "aud";
	private static final String KEY_TOKEN_TYPE = "token_type";
	private static final String KEY_AUTHORITIES = "authorities";
	private static final String KEY_ROLES = "roles";
	private static final String TOKEN_TYPE_ACCESS = "access";
	private static final String TOKEN_TYPE_REFRESH = "refresh";
	private static final String ISSUER = "auth-service";

	private final Key key;
	private final String audience;
	private final long accessSeconds;
	private final long refreshSeconds;

	public AuthJwtTokenService(String secret, String audience, long accessSeconds, long refreshSeconds) {
		if (Strings.isBlank(secret)) {
			throw new AuthException(AuthFailureReason.INVALID_INPUT, "auth.jwt.secret must not be blank");
		}
		if (Strings.isBlank(audience)) {
			throw new AuthException(AuthFailureReason.INVALID_INPUT, "auth.jwt.audience must not be blank");
		}
		byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < 32) {
			throw new AuthException(AuthFailureReason.INVALID_INPUT, "auth.jwt.secret must be at least 32 bytes for HS256");
		}
		this.key = Keys.hmacShaKeyFor(secretBytes);
		this.audience = audience;
		this.accessSeconds = accessSeconds;
		this.refreshSeconds = refreshSeconds;
	}

	@Override
	public String issueAccessToken(Principal principal) {
		return buildToken(principal, accessSeconds, TOKEN_TYPE_ACCESS, true);
	}

	@Override
	public String issueRefreshToken(Principal principal) {
		return buildToken(principal, refreshSeconds, TOKEN_TYPE_REFRESH, false);
	}

	@Override
	public Principal verifyAccessToken(String token) {
		return parseAndToPrincipal(token, TOKEN_TYPE_ACCESS);
	}

	@Override
	public Principal verifyRefreshToken(String token) {
		return parseAndToPrincipal(token, TOKEN_TYPE_REFRESH);
	}

	private String buildToken(Principal principal, long ttlSeconds, String tokenType, boolean includeIssuer) {
		Date issuedAt = new Date();
		Date expiration = new Date(issuedAt.getTime() + (Math.max(ttlSeconds, 1) * 1000L));

		Map<String, Object> claims = new HashMap<>(principal.getAttributes());
		claims.remove(KEY_ISSUER);
		if (!principal.getAuthorities().isEmpty()) {
			claims.put(KEY_AUTHORITIES, principal.getAuthorities());
			claims.put(KEY_ROLES, principal.getAuthorities());
		}

		JwtBuilder builder = Jwts.builder()
			.setSubject(principal.getUserId())
			.setAudience(audience)
			.addClaims(claims)
			.claim(KEY_TOKEN_TYPE, tokenType)
			.setIssuedAt(issuedAt)
			.setExpiration(expiration);

		if (includeIssuer) {
			builder.setIssuer(ISSUER);
		}

		return builder.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	private Principal parseAndToPrincipal(String token, String expectedTokenType) {
		try {
			JwtParser parser = parserBuilder().build();
			Claims claims = parser.parseClaimsJws(token).getBody();

			String tokenType = claims.get(KEY_TOKEN_TYPE, String.class);
			if (tokenType == null || !tokenType.equals(expectedTokenType)) {
				throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid token type");
			}

			String userId = claims.getSubject();
			Map<String, Object> attributes = new HashMap<>(claims);
			attributes.remove("sub");
			attributes.remove("iat");
			attributes.remove("exp");
			attributes.remove(KEY_TOKEN_TYPE);
			attributes.remove(KEY_AUTHORITIES);
			attributes.remove(KEY_ROLES);
			attributes.remove(KEY_ISSUER);

			List<String> authorities = toAuthorities(claims.get(KEY_AUTHORITIES, Object.class));
			if (authorities.isEmpty()) {
				authorities = toAuthorities(claims.get(KEY_ROLES, Object.class));
			}

			return new Principal(userId, authorities, attributes);
		} catch (AuthException ex) {
			throw ex;
		} catch (JwtException | IllegalArgumentException ex) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid/expired token", ex);
		}
	}

	private JwtParserBuilder parserBuilder() {
		return Jwts.parserBuilder().setSigningKey(key);
	}

	private List<String> toAuthorities(Object value) {
		if (value instanceof List<?> list) {
			return list.stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.toList();
		}
		if (value instanceof String string) {
			return List.of(string);
		}
		return List.of();
	}
}
