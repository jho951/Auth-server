package com.authservice.app.domain.auth.userdirectory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "user-service")
public class UserServiceProperties {

	private String baseUrl;
	private String internalApiKey;
}
