package com.authservice.app.domain.auth.sso.controller;

import com.authservice.app.domain.auth.sso.dto.SsoRequest;
import com.authservice.app.domain.auth.sso.dto.SsoResponse;
import com.authservice.app.domain.auth.sso.model.SsoPrincipal;
import com.authservice.app.domain.auth.sso.service.SsoAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class SsoController {

	private final SsoAuthService ssoAuthService;

	public SsoController(SsoAuthService ssoAuthService) {
		this.ssoAuthService = ssoAuthService;
	}

	@GetMapping("/sso/start")
	public ResponseEntity<Void> start(@RequestParam("redirect_uri") String redirectUri) {
		return ResponseEntity.status(302)
			.location(java.net.URI.create(ssoAuthService.buildGithubAuthorizeUrl(redirectUri)))
			.build();
	}

	@GetMapping("/github/callback")
	public ResponseEntity<Void> githubCallback(
		@RequestParam("code") String code,
		@RequestParam("state") String state
	) {
		return ResponseEntity.status(302)
			.location(ssoAuthService.handleGithubCallback(code, state))
			.build();
	}

	@PostMapping("/exchange")
	public ResponseEntity<Void> exchange(@Valid @RequestBody SsoRequest.ExchangeRequest request) {
		return ssoAuthService.exchangeTicket(request.getTicket());
	}

	@GetMapping("/me")
	public SsoResponse.MeResponse me(HttpServletRequest request) {
		SsoPrincipal principal = ssoAuthService.getCurrentUser(request);
		return new SsoResponse.MeResponse(
			principal.getUserId(),
			principal.getEmail(),
			principal.getName(),
			principal.getAvatarUrl(),
			principal.getRoles()
		);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		return ssoAuthService.logout(request);
	}
}
