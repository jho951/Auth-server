package com.authservice.app.domain.auth.userdirectory.service;

import com.authservice.app.common.base.constant.ErrorCode;
import com.authservice.app.common.base.exception.GlobalException;
import com.authservice.app.domain.auth.userdirectory.config.UserServiceProperties;
import com.authservice.app.domain.auth.userdirectory.model.OAuth2ProvisionCommand;
import com.authservice.app.domain.auth.userdirectory.model.UserAccountProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class RemoteUserDirectory implements UserDirectory {

	private final RestClient restClient;
	private final UserServiceProperties userServiceProperties;

	public RemoteUserDirectory(UserServiceProperties userServiceProperties) {
		this.userServiceProperties = userServiceProperties;
		this.restClient = RestClient.builder()
			.baseUrl(userServiceProperties.getBaseUrl() == null ? "" : userServiceProperties.getBaseUrl())
			.build();
	}

	@Override
	public Optional<UserAccountProfile> findByUserId(UUID userId) {
		try {
			UserLookupResponse response = restClient.get()
				.uri("/internal/users/{userId}", userId)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(UserLookupResponse.class);
			return Optional.ofNullable(response).map(UserLookupResponse::toProfile);
		} catch (RestClientException e) {
			throw new GlobalException(ErrorCode.USER_SERVICE_UNAVAILABLE);
		}
	}

	@Override
	public UserAccountProfile provisionOAuth2User(OAuth2ProvisionCommand command) {
		try {
			UserLookupResponse response = restClient.post()
				.uri("/internal/users/social")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(command)
				.retrieve()
				.body(UserLookupResponse.class);

			if (response == null) {
				throw new GlobalException(ErrorCode.USER_SERVICE_UNAVAILABLE);
			}
			return response.toProfile();
		} catch (RestClientException e) {
			throw new GlobalException(ErrorCode.USER_SERVICE_UNAVAILABLE);
		}
	}

	private String bearerToken() {
		return "Bearer " + userServiceProperties.getInternalApiKey();
	}

	private record UserLookupResponse(
		UUID userId,
		String email,
		String name,
		String role,
		String status,
		String avatarUrl
	) {
		private UserAccountProfile toProfile() {
			return new UserAccountProfile(userId, email, name, role, status, avatarUrl);
		}
	}
}
