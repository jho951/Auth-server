package com.authservice.app.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public  class AuthResponse {

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class TokenResponse {
		private String accessToken;
		private String refreshToken;
	}
}
