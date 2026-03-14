package com.authservice.app.domain.sample.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import com.authservice.app.domain.sample.entity.Sample;

public class SampleRequest {

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SampleCreateRequest {
		@NotNull(message = "content is required")
		@Schema(description = "샘플 내용", example = "sample content")
		private String contents;

		public Sample toCreateEntity() {
			return Sample.builder()
				.contents(this.contents)
				.build();
		}
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SampleUpdateRequest {
		@NotNull(message = "id is required")
		@Schema(description = "샘플 도메인의 식별 아이디", example = "1")
		private Long id;

		@NotNull(message = "name is required")
		@Schema(description = "샘플 내용", example = "sample content")
		private String contents;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SampleReadRequest {
		@NotNull(message = "id is required")
		@Schema(description = "샘플 도메인의 식별 아이디", example = "1")
		private Long id;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SampleDeleteRequest {
		@NotNull(message = "id is required")
		@Schema(description = "샘플 도메인의 식별 아이디", example = "1")
		private Long id;

		public Sample toDeleteEntity() {
			return Sample.builder()
				.id(this.id)
				.build();
		}
	}
}