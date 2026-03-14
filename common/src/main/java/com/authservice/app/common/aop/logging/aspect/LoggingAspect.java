package com.authservice.app.common.aop.logging.aspect;

import static com.authservice.app.common.aop.logging.util.LoggingUtil.LogMessageFormatter.*;

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import com.authservice.app.common.aop.logging.util.LoggingUtil;
import com.authservice.app.common.aop.logging.constant.LogLevel;
import com.authservice.app.common.aop.logging.context.MDCManager;
import com.authservice.app.common.aop.logging.annotation.Loggable;

import java.util.Arrays;

/**
 * {@code @Loggable} 어노테이션이 붙은 메서드를 감싸고,
 * 시작/종료/예외 상황을 로그로 출력합니다.
 * 메서드 실행 전: 인자와 함께 START 로그 출력
 * 성공 시: 반환값과 실행 시간 포함한 END 로그 출력
 * 예외 발생 시: 예외 메시지를 ERROR 레벨로 출력
 * 로그 레벨과 타입은 {@link Loggable}의 설정을 기반으로 결정되며,
 * 로그 메시지는 메서드명, 인자, 반환값, 수행 시간 등을 포함합니다.
 * 또한, MDC(Mapped Diagnostic Context)를 활용하여
 * 로그 추적 시 log_type 값을 함께 기록합니다.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
	private static final String MDC_KEY_LOG_TYPE = "log_type";
	/**
	 * @param joinPoint 대상 메서드 실행 정보를 담은 객체
	 * @param loggable  어노테이션에 정의된 로그 설정
	 * @return 원래 메서드의 반환값
	 * @throws Throwable 원래 메서드에서 발생한 예외를 그대로 전달
	 */
	@Around("@annotation(loggable)")
	public Object logMethodCall(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
		String methodName = joinPoint.getSignature().toShortString();
		String argsString = Arrays.toString(joinPoint.getArgs());
		LogLevel level = loggable.level();

		MDCManager.put(MDC_KEY_LOG_TYPE, loggable.type().name());
		LoggingUtil.logByLevel(level, formatStartMessage(methodName, argsString));

		long startTime = System.currentTimeMillis();

		try {
			Object result = joinPoint.proceed();
			long executionTime = System.currentTimeMillis() - startTime;

			LoggingUtil.logByLevel(level, formatEndMessage(methodName, result, executionTime));
			return result;

		} catch (Throwable throwable) {
			LoggingUtil.logByLevel(LogLevel.ERROR, formatExceptionMessage(methodName, throwable));
			throw throwable;
		} finally {
			MDCManager.remove(MDC_KEY_LOG_TYPE);
		}
	}
}
