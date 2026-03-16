package com.authservice.app.domain.auth.userdirectory.config;

import com.authservice.app.common.base.constant.ErrorCode;
import com.authservice.app.common.base.exception.GlobalException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "internal.api")
public class InternalApiProperties {

	private String key;

	public void validateAuthorizationHeader(String authorization) {
		String expected = "Bearer " + key;
		if (key == null || key.isBlank() || !expected.equals(authorization)) {
			throw new GlobalException(ErrorCode.UNAUTHORIZED);
		}
	}
}
