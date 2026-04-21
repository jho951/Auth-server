package com.authservice.app.security;

import com.auth.api.model.Principal;
import com.auth.session.SessionStore;
import com.auth.spi.TokenService;
import com.authservice.app.domain.auth.config.AuthHttpProperties;
import com.authservice.app.domain.auth.model.AuthPrincipal;
import com.authservice.app.domain.auth.sso.model.SsoStorePayloads.SsoSessionPayload;
import com.authservice.app.domain.auth.sso.service.SsoSessionStore;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthPlatformIssuerAdaptersConfiguration {

    @Bean
    public TokenService platformIssuerTokenService(AuthJwtTokenService tokenService) {
        return new TokenService() {
            @Override
            public String issueAccessToken(Principal principal) {
                return tokenService.issueAccessToken(toAuthPrincipal(principal));
            }

            @Override
            public String issueRefreshToken(Principal principal) {
                return tokenService.issueRefreshToken(toAuthPrincipal(principal));
            }

            @Override
            public Principal verifyAccessToken(String token) {
                return toPlatformPrincipal(tokenService.verifyAccessToken(token));
            }

            @Override
            public Principal verifyRefreshToken(String token) {
                return toPlatformPrincipal(tokenService.verifyRefreshToken(token));
            }
        };
    }

    @Bean
    public SessionStore platformIssuerSessionStore(
        SsoSessionStore sessionStore,
        AuthHttpProperties properties
    ) {
        Duration sessionTtl = Duration.ofSeconds(Math.max(1L, properties.getJwt().getRefreshSeconds()));
        return new SessionStore() {
            @Override
            public void save(String sessionId, Principal principal) {
                sessionStore.saveSession(
                    sessionId,
                    new SsoSessionPayload(
                        principal.getUserId(),
                        stringAttribute(principal.getAttributes(), "email"),
                        stringAttribute(principal.getAttributes(), "name"),
                        stringAttribute(principal.getAttributes(), "avatarUrl"),
                        principal.getAuthorities(),
                        stringAttribute(principal.getAttributes(), "status"),
                        Instant.now().plus(sessionTtl)
                    ),
                    Instant.now().plus(sessionTtl)
                );
            }

            @Override
            public java.util.Optional<Principal> find(String sessionId) {
                return sessionStore.findSession(sessionId).map(AuthPlatformIssuerAdaptersConfiguration::toPlatformPrincipal);
            }

            @Override
            public void revoke(String sessionId) {
                sessionStore.revokeSession(sessionId);
            }
        };
    }

    private static AuthPrincipal toAuthPrincipal(Principal principal) {
        return new AuthPrincipal(
            principal.getUserId(),
            principal.getAuthorities(),
            principal.getAttributes()
        );
    }

    private static Principal toPlatformPrincipal(AuthPrincipal principal) {
        return new Principal(principal.userId(), principal.roles(), principal.attributes());
    }

    private static Principal toPlatformPrincipal(SsoSessionPayload payload) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        putIfPresent(attributes, "email", payload.getEmail());
        putIfPresent(attributes, "name", payload.getName());
        putIfPresent(attributes, "avatarUrl", payload.getAvatarUrl());
        putIfPresent(attributes, "status", payload.getStatus());
        return new Principal(payload.getUserId(), payload.getRoles(), attributes);
    }

    private static String stringAttribute(Map<String, Object> attributes, String key) {
        if (attributes == null) {
            return null;
        }
        Object value = attributes.get(key);
        return value instanceof String string && !string.isBlank() ? string : null;
    }

    private static void putIfPresent(Map<String, Object> attributes, String key, String value) {
        if (value != null && !value.isBlank()) {
            attributes.put(key, value);
        }
    }
}
