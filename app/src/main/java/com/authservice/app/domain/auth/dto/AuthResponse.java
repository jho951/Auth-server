package com.authservice.app.domain.auth.dto;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

public  class AuthResponse {
	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TokenResponse {
		private String accessToken;
		private String refreshToken;
	}
}
