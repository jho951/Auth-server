package com.authservice.app.domain.auth.sso.model;

public class GithubUserProfile {

	private final String providerId;
	private final String email;
	private final String name;
	private final String avatarUrl;

	public GithubUserProfile(String providerId, String email, String name, String avatarUrl) {
		this.providerId = providerId;
		this.email = email;
		this.name = name;
		this.avatarUrl = avatarUrl;
	}

	public String getProviderId() {
		return providerId;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}
}
