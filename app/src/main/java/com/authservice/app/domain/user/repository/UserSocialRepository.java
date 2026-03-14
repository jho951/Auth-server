package com.authservice.app.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authservice.app.domain.user.constant.UserSocialType;
import com.authservice.app.domain.user.entity.UserSocial;

public interface UserSocialRepository extends JpaRepository<UserSocial, UUID> {
	Optional<UserSocial> findBySocialTypeAndProviderId(UserSocialType socialType, String providerId);
}
