package com.authservice.app.common.base.config;

import java.util.Optional;

import lombok.NonNull;

import org.springframework.data.domain.AuditorAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;


@Configuration
public class AuditorAwareImpl implements AuditorAware<String> {
	@Override
	@NonNull
	public Optional<String> getCurrentAuditor() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) return Optional.empty();

		return Optional.ofNullable(authentication.getName());
	}
}
