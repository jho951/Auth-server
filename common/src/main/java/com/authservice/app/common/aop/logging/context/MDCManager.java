package com.authservice.app.common.aop.logging.context;

import org.slf4j.MDC;

/**
 * MDC(Mapped Diagnostic Context)를 편리하게 다루기 위한 유틸리티 클래스입니다.
 * 로그에 컨텍스트 정보를 자동으로 포함시키기 위해 SLF4J의 {@link MDC}를 래핑합니다.
 * AOP 로깅 또는 다른 공통 로직에서 로그 유형, 사용자 ID, 요청 ID 등
 * 특정 스레드와 관련된 정보를 로깅에 자동 포함시킬 때 사용됩니다.
 * <strong>주의:</strong> MDC는 ThreadLocal 기반이므로,
 * 작업이 끝나면 반드시 {@code remove()}로 메모리 해제를 해야 합니다.
 */
public class MDCManager {

	/**
	 * MDC key-value 쌍을 설정
	 * @param key   MDC 키 (예: "log_type", "request_id")
	 * @param value 해당 키에 매핑할 문자열 값
	 */
	public static void put(String key, String value) {
		MDC.put(key, value);
	}

	/**
	 * MDC 특정 키 값을 제거
	 * ThreadLocal로 저장된 컨텍스트 정보는 작업 완료 후 반드시 제거해야
	 * 메모리 누수와 잘못된 정보 전파를 방지할 수 있습니다.
	 *
	 * @param key 제거할 MDC 키
	 */
	public static void remove(String key) {
		MDC.remove(key);
	}
}
