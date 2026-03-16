package com.authservice.app.domain.auth.sso.model;

import java.time.Instant;
import java.util.List;

public class SsoStorePayloads {

	public static class SsoStatePayload {
		private String redirectUri;
		private String pageType;
		private Instant expiresAt;

		public SsoStatePayload() {
		}

		public SsoStatePayload(String redirectUri, String pageType, Instant expiresAt) {
			this.redirectUri = redirectUri;
			this.pageType = pageType;
			this.expiresAt = expiresAt;
		}

		public String getRedirectUri() {
			return redirectUri;
		}

		public String getPageType() {
			return pageType;
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
		private String pageType;
		private Instant expiresAt;

		public SsoTicketPayload() {
		}

		public SsoTicketPayload(
			String userId,
			String email,
			String name,
			String avatarUrl,
			List<String> roles,
			String pageType,
			Instant expiresAt
		) {
			this.userId = userId;
			this.email = email;
			this.name = name;
			this.avatarUrl = avatarUrl;
			this.roles = roles;
			this.pageType = pageType;
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

		public String getPageType() {
			return pageType;
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
