package com.authservice.app.common.base.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 오류 종류 : 샘플 도메인 에러 (1000 ~ 1999)
	BAD_REQUEST_SAMPLE_DATA(HttpStatus.BAD_REQUEST, Boolean.FALSE, 1000, "요청이 잘못되었습니다."),
	BAD_RESPONSE_SAMPLE_DATA(HttpStatus.BAD_REQUEST, Boolean.FALSE, 1001, "입력이 잘못되었습니다."),
	UNAUTHORIZED_SAMPLE_DATA(HttpStatus.UNAUTHORIZED,Boolean.FALSE,1002,"인증되지 않았습니다."),
	NOT_FOUND_SAMPLE_DATA(HttpStatus.NOT_FOUND, Boolean.FALSE,1003,"찾는 데이터가 존재하지 않습니다."),
	NOT_FOUND_SAMPLE_DATA_ID(HttpStatus.NOT_FOUND, Boolean.FALSE,1004,"찾는 데이터의 아이디가 존재하지 않습니다."),
	CONFLICT_SAMPLE_DATA(HttpStatus.CONFLICT,Boolean.FALSE,1005,"다른 사용자가 데이터를 수정했습니다. 다시 시도해주세요."),

	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, Boolean.FALSE, 9010, "인증 정보가 없습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED,  Boolean.FALSE, 9011, "유효하지 않은 인증 토큰입니다."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,  Boolean.FALSE, 9012, "인증 토큰이 만료되었습니다."),
	NEED_LOGIN(HttpStatus.UNAUTHORIZED,  Boolean.FALSE, 9013, "로그인이 필요한 요청입니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN,  Boolean.FALSE, 9014, "해당 요청에 대한 권한이 없습니다."),

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, Boolean.FALSE, 9015, "잘못된 요청입니다."),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, Boolean.FALSE, 9016, "요청 필드 유효성 검사에 실패했습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, Boolean.FALSE,9017, "허용되지 않은 HTTP 메서드입니다."),

	// 오류 종류 : 인증 에러(3000~3999)
	INVALID_TOKEN_TYPE(HttpStatus.BAD_REQUEST, Boolean.FALSE, 3000, "토큰 타입이 잘못되었습니다."),
	INVALID_USER_ID(HttpStatus.BAD_REQUEST, Boolean.FALSE, 3001, "유저 아이디가 잘못되었습니다."),
	INVALID_JWT_ALGORITHM(HttpStatus.BAD_REQUEST, Boolean.FALSE, 3002, "JJWT 지원하는 알고리즘이 잘못되었습니다."),

	//오류 종류 : 공통 에러 (9000 ~ 9999)
	VALIDATION_FAIL_ERROR(HttpStatus.BAD_REQUEST, Boolean.FALSE, 9000, "(exception error 메세지에 따름)"),
	NOT_SUPPORTED_METHOD(HttpStatus.METHOD_NOT_ALLOWED, Boolean.FALSE, 9001, "(exception error 메세지에 따름"),
	NOT_FOUND_URL(HttpStatus.NOT_FOUND, Boolean.FALSE, 9002, "요청하신 URL 을 찾을 수 없습니다."),
	INVALID_REQUEST_DATA(HttpStatus.BAD_REQUEST, Boolean.FALSE, 9003, "데이터 저장 실패, 재시도 혹은 관리자에게 문의해주세요."),

	NOT_FOUND_USER(HttpStatus.NOT_FOUND, Boolean.FALSE, 9004, "요청하신 유저를 찾을 수 없습니다."),
	FAIL(HttpStatus.BAD_REQUEST, Boolean.FALSE, 9999, "요청 응답 실패, 관리자에게 문의해주세요.");

	private final HttpStatus httpStatus;
	private final Boolean isSuccess;
	private final int code;
	private final String message;

}
