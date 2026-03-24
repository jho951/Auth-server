package com.authservice.app.domain.auth.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class InternalAuthRequest {

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CreateAccountRequest {
		@NotNull
		@Schema(description = "user-service user id")
		private UUID userId;

		@Email
		@NotBlank
		@Schema(description = "로그인 아이디")
		private String loginId;

		@NotBlank
		@Size(min = 8, max = 72)
		@Schema(description = "평문 비밀번호")
		private String password;
	}
}
