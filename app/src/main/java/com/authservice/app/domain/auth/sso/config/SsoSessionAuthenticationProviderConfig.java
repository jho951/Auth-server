package com.authservice.app.domain.auth.sso.config;

import com.auth.api.model.Principal;
import com.auth.session.SessionAuthenticationProvider;
import com.authservice.app.domain.auth.sso.service.SsoSessionStore;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SsoSessionAuthenticationProviderConfig {

    private static final List<String> NO_ROLES = List.of();

    @Bean
    public SessionAuthenticationProvider sessionAuthenticationProvider(SsoSessionStore sessionStore) {
        return sessionId -> sessionStore.findSession(sessionId)
            .map(payload -> new Principal(
                payload.getUserId(),
                payload.getRoles() == null ? NO_ROLES : List.copyOf(payload.getRoles())
            ));
    }
}
