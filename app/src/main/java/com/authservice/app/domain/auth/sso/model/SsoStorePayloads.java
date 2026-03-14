package com.authservice.app.domain.auth.sso.model;

import java.time.Instant;
import java.util.List;

public class SsoStorePayloads {

	public static class SsoStatePayload {
		private String redirectUri;
		private Instant expiresAt;

		public SsoStatePayload() {
		}

		public SsoStatePayload(String redirectUri, Instant expiresAt) {
			this.redirectUri = redirectUri;
			this.expiresAt = expiresAt;
		}

		public String getRedirectUri() {
			return redirectUri;
		}

		public Instant getExpiresAt() {
			return expiresAt;
		}
	}

	public static class SsoTicketPayload {
		private String userId;
		private String email;
		private String name;
		private String avatarUrl;
		private List<String> roles;
		private Instant expiresAt;

		public SsoTicketPayload() {
		}

		public SsoTicketPayload(String userId, String email, String name, String avatarUrl, List<String> roles, Instant expiresAt) {
			this.userId = userId;
			this.email = email;
			this.name = name;
			this.avatarUrl = avatarUrl;
			this.roles = roles;
			this.expiresAt = expiresAt;
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

		public Instant getExpiresAt() {
			return expiresAt;
		}
	}

	public static class SsoSessionPayload {
		private String userId;
		private String email;
		private String name;
		private String avatarUrl;
		private List<String> roles;
		private Instant expiresAt;

		public SsoSessionPayload() {
		}

		public SsoSessionPayload(String userId, String email, String name, String avatarUrl, List<String> roles, Instant expiresAt) {
			this.userId = userId;
			this.email = email;
			this.name = name;
			this.avatarUrl = avatarUrl;
			this.roles = roles;
			this.expiresAt = expiresAt;
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

		public Instant getExpiresAt() {
			return expiresAt;
		}
	}
}
