package com.authservice.app.common.aop.logging.constant;

import com.authservice.app.common.aop.logging.annotation.Loggable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 로그의 유형을 나타내는 열거형(Enum)입니다.
 * {@link Loggable} 어노테이션에서 사용되며,
 * 로그를 출력할 때 해당 로그가 어떤 카테고리에 속하는지를 구분하는 데 사용됩니다.
 * {@code log_type}이라는 MDC(Mapped Diagnostic Context) 키에 설정되어,
 * 로그 분석 및 필터링 시 활용됩니다.
 */
@Getter
@RequiredArgsConstructor
public enum LogType {

	/**
	 * API 호출 관련 로그 (예: 컨트롤러 진입, 요청 처리 등)
	 */
	API("api"),

	/**
	 * 시스템 내부 처리 관련 로그 (예: 배치, 비즈니스 로직 등)
	 */
	SYSTEM("system"),

	/**
	 * 기본 로그 유형, 명시되지 않은 기타 케이스
	 */
	DEFAULT("default");

	/**
	 * 로그 유형에 대한 문자열 표현 (예: "api", "system")
	 */
	private final String type;
}
