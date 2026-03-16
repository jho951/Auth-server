package com.authservice.app.common.aop.logging.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import com.authservice.app.common.aop.logging.aspect.LoggingAspect;
import com.authservice.app.common.aop.logging.constant.LogType;
import com.authservice.app.common.aop.logging.constant.LogLevel;
import com.authservice.app.common.aop.logging.util.LoggingUtil;

/**
 * 메서드 또는 클래스에 부착하여 AOP 기반의 로깅을 적용
 * {@code @Loggable}이 적용된 대상은 {@link LoggingAspect}에 의해
 * 자동으로 로그가 출력됩니다.
 * 로그 레벨과 로그 유형을 세부적으로 설정할 수 있으며,
 * 메서드 실행 전/후, 예외 발생 시 로그가 기록됩니다.
 * 적용 대상:
 * 메서드: {@code @Loggable}을 메서드에 직접 선언
 * 클래스: 클래스 내 모든 메서드에 AOP 로깅 적용됨
 * 유지 정책: 런타임까지 유지되어 리플렉션 및 AOP 프레임워크에서 참조됩니다.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {

	/**
	 * 로그 출력 레벨을 설정합니다.
	 * 기본값은 {@link LogLevel#DEBUG}이며,
	 * {@link LoggingUtil}을 통해 해당 레벨로 로그가 출력됩니다.
	 *
	 * @return 로그 레벨 (TRACE, DEBUG, INFO, WARN, ERROR)
	 */
	LogLevel level() default LogLevel.DEBUG;

	/**
	 * 로그 유형을 설정합니다.
	 * 이 값은 MDC(Mapped Diagnostic Context)에 {@code log_type}으로 저장되어,
	 * 로그 필터링이나 분석 시 유형 구분에 사용됩니다.
	 *
	 * @return 로그 타입 (예: API, SYSTEM, DEFAULT)
	 */
	LogType type() default LogType.DEFAULT;
}
