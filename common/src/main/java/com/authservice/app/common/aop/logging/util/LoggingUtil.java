package com.authservice.app.common.aop.logging.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.authservice.app.common.aop.logging.constant.LogLevel;

/**
 * 로그 출력에 대한 유틸
 * {@link LogLevel}에 따라 로그를 해당 레벨로 출력하는 메서드를 제공합니다.
 * 내부 정적 클래스 {@link LogMessageFormatter}는 로그 메시지 포맷을 구성하는 역할을 담당합니다.
 */
public class LoggingUtil {

	private static final Logger log = LoggerFactory.getLogger(LoggingUtil.class);

	/**
	 * 지정된 {@link LogLevel}에 따라 로그를 출력
	 * @param level   로그 레벨 (TRACE, DEBUG, INFO, WARN, ERROR)
	 * @param message 출력할 메시지
	 */
	public static void logByLevel(LogLevel level, String message) {
		switch (level) {
			case TRACE -> log.trace(message);
			case DEBUG -> log.debug(message);
			case INFO  -> log.info(message);
			case WARN  -> log.warn(message);
			case ERROR -> log.error(message);
		}
	}

	/**
	 * 로그 메시지 포맷 생성을 위한 정적 유틸
	 * 메서드 실행의 시작, 종료, 예외 상황에 대한 메시지 포맷을 일관된 형태로 제공합니다.
	 */
	public static class LogMessageFormatter {

		/**
		 * 메서드 실행 시작 시 메시지 생성
		 *
		 * @param methodName 메서드 이름
		 * @param args       인자 정보
		 * @return 포맷팅된 시작 메시지
		 */
		public static String formatStartMessage(String methodName, String args) {
			return "[START] " + methodName + " args=" + args;
		}

		/**
		 * 메서드 실행 완료 후 메시지 생성
		 *
		 * @param methodName     메서드 이름
		 * @param result         반환값
		 * @param timeMs         실행 소요 시간 (ms 단위)
		 * @return 포맷팅된 종료 메시지
		 */
		public static String formatEndMessage(String methodName, Object result, long timeMs) {
			return String.format("[END] %s return=%s (time=%dms)", methodName, result, timeMs);
		}

		/**
		 * 예외 발생 시 메시지 생성
		 *
		 * @param methodName 메서드 이름
		 * @param ex         발생한 예외 객체
		 * @return 포맷팅된 예외 메시지
		 */
		public static String formatExceptionMessage(String methodName, Throwable ex) {
			return String.format("[EXCEPTION] %s ex=%s", methodName, ex.toString());
		}
	}
}
