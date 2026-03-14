package com.authservice.app.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 요청 DTO들을 클래스 형태로 묶어두는 컨테이너.
 * Jackson 역직렬화를 위해 각 내부 클래스는
 * - 기본 생성자(@NoArgsConstructor)
 * - 게터(@Getter)
 * - 세터(@Setter) 를 가집니다.
 */
public class AuthRequest {

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LoginRequest {
		@Email
		@NotBlank
		private String username;

		@NotBlank
		@Size(min = 8, max = 72)
		private String password;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RefreshRequest {
		@NotBlank
		private String refreshToken;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LogoutRequest {
		@NotBlank
		private String refreshToken;
	}
}
