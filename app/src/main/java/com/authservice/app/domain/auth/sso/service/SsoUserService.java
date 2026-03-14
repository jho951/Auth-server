package com.authservice.app.domain.auth.sso.service;

import com.authservice.app.domain.auth.sso.model.GithubUserProfile;
import com.authservice.app.domain.auth.sso.model.SsoPrincipal;
import com.authservice.app.domain.user.constant.UserRole;
import com.authservice.app.domain.user.constant.UserSocialType;
import com.authservice.app.domain.user.constant.UserStatus;
import com.authservice.app.domain.user.entity.User;
import com.authservice.app.domain.user.entity.UserSocial;
import com.authservice.app.domain.user.repository.UserRepository;
import com.authservice.app.domain.user.repository.UserSocialRepository;
import com.authservice.app.common.base.constant.ErrorCode;
import com.authservice.app.common.base.exception.GlobalException;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SsoUserService {

	private final UserRepository userRepository;
	private final UserSocialRepository userSocialRepository;

	public SsoUserService(UserRepository userRepository, UserSocialRepository userSocialRepository) {
		this.userRepository = userRepository;
		this.userSocialRepository = userSocialRepository;
	}

	@Transactional
	public SsoPrincipal verifyGithubUser(GithubUserProfile profile) {
		User user = userSocialRepository.findBySocialTypeAndProviderId(UserSocialType.GITHUB, profile.getProviderId())
			.map(UserSocial::getUser)
			.orElseGet(() -> findOrCreateUser(profile));

		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new GlobalException(ErrorCode.FORBIDDEN);
		}

		return new SsoPrincipal(
			user.getId().toString(),
			user.getEmail(),
			user.getName(),
			profile.getAvatarUrl(),
			List.of(user.getRole().getValue().toLowerCase(Locale.ROOT))
		);
	}

	private User findOrCreateUser(GithubUserProfile profile) {
		return userRepository.findByEmail(profile.getEmail())
			.map(existing -> linkGithub(profile, existing))
			.orElseGet(() -> createGithubUser(profile));
	}

	private User linkGithub(GithubUserProfile profile, User user) {
		userSocialRepository.findBySocialTypeAndProviderId(UserSocialType.GITHUB, profile.getProviderId())
			.or(() -> {
				UserSocial social = UserSocial.builder()
					.user(user)
					.socialType(UserSocialType.GITHUB)
					.providerId(profile.getProviderId())
					.build();
				return java.util.Optional.of(userSocialRepository.save(social));
			});
		return user;
	}

	private User createGithubUser(GithubUserProfile profile) {
		User user = userRepository.save(User.builder()
			.email(profile.getEmail())
			.name(profile.getName())
			.role(UserRole.ROLE_USER)
			.status(UserStatus.ACTIVE)
			.build());

		userSocialRepository.save(UserSocial.builder()
			.user(user)
			.socialType(UserSocialType.GITHUB)
			.providerId(profile.getProviderId())
			.build());

		return user;
	}
}
