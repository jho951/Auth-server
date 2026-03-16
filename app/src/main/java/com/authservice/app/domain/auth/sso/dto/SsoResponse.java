package com.authservice.app.domain.auth.sso.dto;

import java.util.List;

public class SsoResponse {

	public static class MeResponse {
		private final String id;
		private final String email;
		private final String name;
		private final String avatarUrl;
		private final List<String> roles;

		public MeResponse(String id, String email, String name, String avatarUrl, List<String> roles) {
			this.id = id;
			this.email = email;
			this.name = name;
			this.avatarUrl = avatarUrl;
			this.roles = roles;
		}

		public String getId() {
			return id;
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
}
