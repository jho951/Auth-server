package com.authservice.app.domain.auth.userdirectory.service;

import com.authservice.app.domain.auth.userdirectory.model.OAuth2ProvisionCommand;
import com.authservice.app.domain.auth.userdirectory.model.UserAccountProfile;
import java.util.Optional;
import java.util.UUID;

public interface UserDirectory {
	Optional<UserAccountProfile> findByUserId(UUID userId);
	UserAccountProfile provisionOAuth2User(OAuth2ProvisionCommand command);
}
