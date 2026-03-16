package com.authservice.app.domain.auth.sso.service;

import com.auth.api.model.OAuth2UserIdentity;
import com.auth.api.model.Principal;
import com.authservice.app.domain.auth.sso.model.SsoPrincipal;
import com.auth.spi.OAuth2PrincipalResolver;
import org.springframework.stereotype.Component;

@Component
public class AuthModuleOAuth2PrincipalResolver implements OAuth2PrincipalResolver {

	private final SsoUserService ssoUserService;

	public AuthModuleOAuth2PrincipalResolver(SsoUserService ssoUserService) {
		this.ssoUserService = ssoUserService;
	}

	@Override
	public Principal resolve(OAuth2UserIdentity identity) {
		SsoPrincipal principal = ssoUserService.verifyOAuth2User(identity);
		return new Principal(principal.getUserId(), principal.getRoles());
	}
}
