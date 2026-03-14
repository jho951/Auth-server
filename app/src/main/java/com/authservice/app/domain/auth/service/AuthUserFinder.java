package com.authservice.app.domain.auth.service;

import com.auth.api.model.User;
import com.auth.spi.UserFinder;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.authservice.app.domain.auth.repository.AuthRepository;
import com.authservice.app.domain.user.constant.UserRole;
import com.authservice.app.domain.user.constant.UserStatus;
import com.authservice.app.domain.user.repository.UserRepository;

@Component
public class AuthUserFinder implements UserFinder {

	private final UserRepository userRepository;
	private final AuthRepository authRepository;

	public AuthUserFinder(UserRepository userRepository, AuthRepository authRepository) {
		this.userRepository = userRepository;
		this.authRepository = authRepository;
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return userRepository.findByEmail(username)
			.filter(user -> user.getStatus() == UserStatus.ACTIVE)
			.flatMap(user -> authRepository.findByUserId(user.getId())
				.map(auth -> new User(
					String.valueOf(user.getId()),
					user.getEmail(),
					auth.getPasswordHash(),
					mapRoles(user.getRole())
				)));
	}

	private List<String> mapRoles(UserRole role) {
		if (role == null) {
			return List.of();
		}
		return List.of(role.name());
	}
}
