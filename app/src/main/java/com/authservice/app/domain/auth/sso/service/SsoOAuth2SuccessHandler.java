package com.authservice.app.domain.auth.sso.service;

import com.auth.api.model.OAuth2UserIdentity;
import com.authservice.app.domain.auth.sso.model.SsoPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class SsoOAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final SsoAuthService ssoAuthService;
	private final SsoUserService ssoUserService;
	private final SsoCookieService ssoCookieService;

	public SsoOAuth2SuccessHandler(
		SsoAuthService ssoAuthService,
		SsoUserService ssoUserService,
		SsoCookieService ssoCookieService
	) {
		this.ssoAuthService = ssoAuthService;
		this.ssoUserService = ssoUserService;
		this.ssoCookieService = ssoCookieService;
	}

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) throws IOException, ServletException {
		if (!(authentication instanceof OAuth2AuthenticationToken token)) {
			throw new ServletException("Unsupported authentication type: " + authentication.getClass().getName());
		}

		OAuth2AuthenticatedPrincipal oauthPrincipal = token.getPrincipal();
		OAuth2UserIdentity identity = new OAuth2UserIdentity(
			token.getAuthorizedClientRegistrationId(),
			oauthPrincipal.getName(),
			readString(oauthPrincipal, "email"),
			readString(oauthPrincipal, "name"),
			oauthPrincipal.getAttributes()
		);

		SsoPrincipal principal = ssoUserService.verifyOAuth2User(identity);
		URI redirectUri = ssoAuthService.completeOAuthLogin(principal, request);

		response.setStatus(HttpServletResponse.SC_FOUND);
		response.setHeader("Location", redirectUri.toString());
		response.addHeader("Set-Cookie", ssoCookieService.clearOAuthStateCookie());
	}

	private String readString(OAuth2AuthenticatedPrincipal principal, String key) {
		Object value = principal.getAttributes().get(key);
		return value == null ? null : String.valueOf(value);
	}
}
