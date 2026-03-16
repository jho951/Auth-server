package com.authservice.app.domain.auth.sso.model;

import com.authservice.app.common.base.constant.ErrorCode;
import com.authservice.app.common.base.exception.GlobalException;
import java.util.Locale;

public enum SsoPageType {
	EXPLAIN,
	EDITOR,
	ADMIN;

	public static SsoPageType from(String value) {
		if (value == null || value.isBlank()) {
			throw new GlobalException(ErrorCode.INVALID_REQUEST);
		}

		try {
			return SsoPageType.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new GlobalException(ErrorCode.INVALID_REQUEST);
		}
	}
}
