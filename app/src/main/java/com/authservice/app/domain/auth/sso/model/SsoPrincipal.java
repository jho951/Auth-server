package com.authservice.app.domain.auth.sso.model;

import java.util.List;

public class SsoPrincipal {

	private final String userId;
	private final String email;
	private final String name;
	private final String avatarUrl;
	private final List<String> roles;

	public SsoPrincipal(String userId, String email, String name, String avatarUrl, List<String> roles) {
		this.userId = userId;
		this.email = email;
		this.name = name;
		this.avatarUrl = avatarUrl;
		this.roles = roles;
	}

	public String getUserId() {
		return userId;
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

	public List<String> getRoles() {
		return roles;
	}
}
