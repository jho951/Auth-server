package com.authservice.app.domain.user.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
	ROLE_USER("USER"),
	ROLE_ADMIN("ADMIN");

	private final String value;
}
