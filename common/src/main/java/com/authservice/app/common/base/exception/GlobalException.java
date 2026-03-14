package com.authservice.app.common.base.exception;

import lombok.Getter;
import com.authservice.app.common.base.constant.ErrorCode;

@Getter
public class GlobalException extends RuntimeException {

	private final ErrorCode errorCode;

	public GlobalException(final ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}