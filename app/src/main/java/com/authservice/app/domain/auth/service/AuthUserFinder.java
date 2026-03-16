package com.authservice.app.domain.auth.service;

import com.auth.api.model.User;
import com.auth.spi.UserFinder;
import com.authservice.app.domain.auth.userdirectory.service.UserDirectory;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.authservice.app.domain.auth.repository.AuthRepository;

@Component
public class AuthUserFinder implements UserFinder {

	private final AuthRepository authRepository;
	private final UserDirectory userDirectory;

	public AuthUserFinder(AuthRepository authRepository, UserDirectory userDirectory) {
		this.authRepository = authRepository;
		this.userDirectory = userDirectory;
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return authRepository.findByUsername(username)
			.flatMap(auth -> userDirectory.findByUserId(auth.getUserId())
				.filter(user -> "ACTIVE".equalsIgnoreCase(user.status()))
				.map(user -> new User(
					String.valueOf(user.userId()),
					auth.getUsername(),
					auth.getPasswordHash(),
					List.of(user.role())
				)));
	}
}
