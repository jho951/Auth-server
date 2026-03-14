package com.authservice.app.domain.sample.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

import com.authservice.app.domain.sample.entity.Sample;
import com.authservice.app.common.base.dto.BaseResponse;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

public class SampleResponse {

	@Getter
	@SuperBuilder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SampleCreateResponse  {
		@NotBlank(message ="content is required")
		@Schema(description = "샘플 내용", example = "1")
		private String content;

		public static SampleCreateResponse from(Sample sample) {
			return SampleCreateResponse.builder()
				.content(sample.getContents())
				.build();
		}
	}

	@Getter
	@SuperBuilder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SampleUpdateResponse {
		@NotEmpty(message ="content is required")
		private String content;

		@Version
		private Long version;

		public static SampleUpdateResponse from(Sample sample) {
			return SampleUpdateResponse.builder()
				.content(sample.getContents())
				.build();
		}
	}

	@Getter
	@SuperBuilder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SampleReadResponse extends BaseResponse {
		@NotEmpty(message ="id is required")
		private Long id;

		@NotEmpty(message ="content is required")
		private String content;

		@Version
		private Long version;

		public static SampleReadResponse from(Sample sample) {
			return SampleReadResponse.builder()
				.id(sample.getId())
				.content(sample.getContents())
				.createdAt(sample.getCreateDate())
				.updatedAt(sample.getModifiedDate())
				.build();
		}
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SampleReadListResponse {
		private List<SampleReadResponse> samples;

		public static SampleReadListResponse from(List<Sample> sampleList) {
			List<SampleReadResponse> dtoList = sampleList.stream()
				.map(SampleReadResponse::from)
				.collect(Collectors.toList());
			return SampleReadListResponse.builder()
				.samples(dtoList)
				.build();
		}
		public static SampleReadListResponse from(Sample sample) {
			return SampleReadListResponse.builder()
				.samples(Collections.singletonList(SampleReadResponse.from(sample)))
				.build();
		}
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SampleDeleteResponse  {
		@NotEmpty(message ="name is required")
		private Long id;

		public static SampleDeleteResponse from(Sample sample) {
			return SampleDeleteResponse.builder()
				.id(sample.getId())
				.build();
		}
	}
}