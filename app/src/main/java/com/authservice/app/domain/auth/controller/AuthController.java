package com.authservice.app.domain.auth.controller;

import com.auth.api.model.Tokens;
import com.auth.config.controller.RefreshCookieWriter;
import com.auth.config.controller.RefreshTokenExtractor;
import com.auth.config.dto.LoginResponse;
import com.auth.core.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authservice.app.domain.auth.dto.AuthRequest;
import com.authservice.app.domain.auth.dto.AuthResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final RefreshTokenExtractor refreshTokenExtractor;
	private final RefreshCookieWriter refreshCookieWriter;

	public AuthController(
		AuthService authService,
		RefreshTokenExtractor refreshTokenExtractor,
		RefreshCookieWriter refreshCookieWriter
	) {
		this.authService = authService;
		this.refreshTokenExtractor = refreshTokenExtractor;
		this.refreshCookieWriter = refreshCookieWriter;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse.TokenResponse> login(@Valid @RequestBody AuthRequest.LoginRequest req) {
		Tokens tokens = authService.login(req.getUsername(), req.getPassword());
		ResponseEntity<LoginResponse> response = refreshCookieWriter.write(
			tokens,
			ResponseEntity.ok(new LoginResponse(tokens.getAccessToken()))
		);
		return ResponseEntity.status(response.getStatusCode())
			.headers(response.getHeaders())
			.body(new AuthResponse.TokenResponse(tokens.getAccessToken(), tokens.getRefreshToken()));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse.TokenResponse> refresh(HttpServletRequest request) {
		String refreshToken = refreshTokenExtractor.extract(request);
		Tokens tokens = authService.refresh(refreshToken);
		ResponseEntity<LoginResponse> response = refreshCookieWriter.write(
			tokens,
			ResponseEntity.ok(new LoginResponse(tokens.getAccessToken()))
		);
		return ResponseEntity.status(response.getStatusCode())
			.headers(response.getHeaders())
			.body(new AuthResponse.TokenResponse(tokens.getAccessToken(), tokens.getRefreshToken()));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		String refreshToken = refreshTokenExtractor.extract(request);
		authService.logout(refreshToken);
		return refreshCookieWriter.clear(ResponseEntity.noContent().build());
	}
}
