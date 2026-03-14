package com.authservice.app.domain.user.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
	ACTIVE("활성 사용자"),
	DELETE("삭제된 사용자"),
	BLOCK("차단된 사용자");

	private final String description;
}
