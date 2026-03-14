package com.authservice.app.domain.auth.service;

import com.auth.spi.PasswordVerifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthPasswordVerifier implements PasswordVerifier {

	private final PasswordEncoder passwordEncoder;

	public AuthPasswordVerifier(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public boolean matches(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}
}
