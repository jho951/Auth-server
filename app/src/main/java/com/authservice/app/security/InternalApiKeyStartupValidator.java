package com.authservice.app.security;

import com.authservice.app.domain.auth.userdirectory.config.InternalApiProperties;
import java.util.Arrays;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class InternalApiKeyStartupValidator implements ApplicationRunner {

	private static final String LOCAL_INTERNAL_API_KEY = "local-internal-api-key";

	private final Environment environment;
	private final InternalApiProperties internalApiProperties;

	public InternalApiKeyStartupValidator(Environment environment, InternalApiProperties internalApiProperties) {
		this.environment = environment;
		this.internalApiProperties = internalApiProperties;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!isProdProfile()) {
			return;
		}
		String key = internalApiProperties.getKey();
		if (key == null || key.isBlank() || LOCAL_INTERNAL_API_KEY.equals(key)) {
			throw new IllegalStateException("INTERNAL_API_KEY must be explicitly configured in prod");
		}
	}

	private boolean isProdProfile() {
		return Arrays.asList(environment.getActiveProfiles()).contains("prod");
	}
}
