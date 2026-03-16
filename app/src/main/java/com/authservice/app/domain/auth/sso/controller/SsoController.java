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
	public ResponseEntity<Void> start(
		@RequestParam(name = "page", required = false) String page,
		@RequestParam(name = "redirect_uri", required = false) String redirectUri,
		HttpServletRequest request
	) {
		return ssoAuthService.startGithubLogin(page, redirectUri, request);
	}

	@PostMapping("/exchange")
	public ResponseEntity<Void> exchange(@Valid @RequestBody SsoRequest.ExchangeRequest request, HttpServletRequest servletRequest) {
		return ssoAuthService.exchangeTicket(request.getTicket(), servletRequest);
	}

	@GetMapping("/me")
	public SsoResponse.MeResponse me(
		HttpServletRequest request,
		@RequestParam(name = "page", required = false) String page
	) {
		SsoPrincipal principal = ssoAuthService.getCurrentUser(request, page);
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
