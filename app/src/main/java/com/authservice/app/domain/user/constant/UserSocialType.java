package com.authservice.app.domain.user.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserSocialType {
	GITHUB("깃허브"),
	GOOGLE("구글"),
	KAKAO("카카오"),
	DEFAULT("기본");

	private final String value;
}
