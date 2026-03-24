package com.authservice.app.domain.auth.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** 인증 관련 요청 DTO 클래스 */
public class AuthRequest {

	/** 로그인 */
	public static class LoginRequest {
		@Email
		@NotBlank
		private String username;

		@NotBlank
		@Size(min = 8, max = 72)
		private String password;

		public LoginRequest() {}

		/**
		 * 생성자
		 * @param username 아이디
		 * @param password 비밀번호
		 */
		public LoginRequest(String username, String password) {
			this.username = username;
			this.password = password;
		}

		public String getUsername() {return username;}
		public void setUsername(String username) {this.username = username;}
		public String getPassword() {return password;}
		public void setPassword(String password) {this.password = password;}
	}

	/** 토큰 갱신 */
	public static class RefreshRequest {
		@NotBlank
		private String refreshToken;

		public RefreshRequest() {}

		/**
		 * 생성자
		 * @param refreshToken 토큰 갱신을 위한 refreshToken
		 */
		public RefreshRequest(String refreshToken) {
			this.refreshToken = refreshToken;
		}

		public String getRefreshToken() {return refreshToken;}
		public void setRefreshToken(String refreshToken) {this.refreshToken = refreshToken;}
	}

	/** 로그아웃 */
	public static class LogoutRequest {
		@NotBlank
		private String refreshToken;

		public LogoutRequest() {}

		/**
		 * 생성자
		 * @param refreshToken 토큰 갱신을 위한 refreshToken
		 */
		public LogoutRequest(String refreshToken) {
			this.refreshToken = refreshToken;
		}

		public String getRefreshToken() {return refreshToken;}
		public void setRefreshToken(String refreshToken) {this.refreshToken = refreshToken;}
	}
}