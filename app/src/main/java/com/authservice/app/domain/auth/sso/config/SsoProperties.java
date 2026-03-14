package com.authservice.app.domain.auth.sso.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sso")
public class SsoProperties {

	private final Github github = new Github();
	private final Frontend frontend = new Frontend();
	private final Session session = new Session();
	private long stateTtlSeconds = 300;
	private long ticketTtlSeconds = 120;

	public Github getGithub() {
		return github;
	}

	public Frontend getFrontend() {
		return frontend;
	}

	public Session getSession() {
		return session;
	}

	public long getStateTtlSeconds() {
		return stateTtlSeconds;
	}

	public void setStateTtlSeconds(long stateTtlSeconds) {
		this.stateTtlSeconds = stateTtlSeconds;
	}

	public long getTicketTtlSeconds() {
		return ticketTtlSeconds;
	}

	public void setTicketTtlSeconds(long ticketTtlSeconds) {
		this.ticketTtlSeconds = ticketTtlSeconds;
	}

	public static class Github {
		private String clientId;
		private String clientSecret;
		private String callbackUri;
		private String authorizeUri = "https://github.com/login/oauth/authorize";
		private String tokenUri = "https://github.com/login/oauth/access_token";
		private String userUri = "https://api.github.com/user";
		private String emailsUri = "https://api.github.com/user/emails";
		private List<String> scopes = new ArrayList<>(List.of("read:user", "user:email"));

		public String getClientId() {
			return clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public String getClientSecret() {
			return clientSecret;
		}

		public void setClientSecret(String clientSecret) {
			this.clientSecret = clientSecret;
		}

		public String getCallbackUri() {
			return callbackUri;
		}

		public void setCallbackUri(String callbackUri) {
			this.callbackUri = callbackUri;
		}

		public String getAuthorizeUri() {
			return authorizeUri;
		}

		public void setAuthorizeUri(String authorizeUri) {
			this.authorizeUri = authorizeUri;
		}

		public String getTokenUri() {
			return tokenUri;
		}

		public void setTokenUri(String tokenUri) {
			this.tokenUri = tokenUri;
		}

		public String getUserUri() {
			return userUri;
		}

		public void setUserUri(String userUri) {
			this.userUri = userUri;
		}

		public String getEmailsUri() {
			return emailsUri;
		}

		public void setEmailsUri(String emailsUri) {
			this.emailsUri = emailsUri;
		}

		public List<String> getScopes() {
			return scopes;
		}

		public void setScopes(List<String> scopes) {
			this.scopes = scopes;
		}
	}

	public static class Frontend {
		private List<String> allowedOrigins = new ArrayList<>(List.of(
			"http://localhost:3000",
			"http://localhost:5173"
		));
		private List<String> allowedRedirectUris = new ArrayList<>(List.of("http://localhost:5173/auth/callback"));

		public List<String> getAllowedOrigins() {
			return allowedOrigins;
		}

		public void setAllowedOrigins(List<String> allowedOrigins) {
			this.allowedOrigins = allowedOrigins;
		}

		public List<String> getAllowedRedirectUris() {
			return allowedRedirectUris;
		}

		public void setAllowedRedirectUris(List<String> allowedRedirectUris) {
			this.allowedRedirectUris = allowedRedirectUris;
		}
	}

	public static class Session {
		private String cookieName = "sso_session";
		private boolean cookieSecure;
		private boolean cookieHttpOnly = true;
		private String cookieSameSite = "Lax";
		private String cookiePath = "/";
		private long ttlSeconds = 604800;

		public String getCookieName() {
			return cookieName;
		}

		public void setCookieName(String cookieName) {
			this.cookieName = cookieName;
		}

		public boolean isCookieSecure() {
			return cookieSecure;
		}

		public void setCookieSecure(boolean cookieSecure) {
			this.cookieSecure = cookieSecure;
		}

		public boolean isCookieHttpOnly() {
			return cookieHttpOnly;
		}

		public void setCookieHttpOnly(boolean cookieHttpOnly) {
			this.cookieHttpOnly = cookieHttpOnly;
		}

		public String getCookieSameSite() {
			return cookieSameSite;
		}

		public void setCookieSameSite(String cookieSameSite) {
			this.cookieSameSite = cookieSameSite;
		}

		public String getCookiePath() {
			return cookiePath;
		}

		public void setCookiePath(String cookiePath) {
			this.cookiePath = cookiePath;
		}

		public long getTtlSeconds() {
			return ttlSeconds;
		}

		public void setTtlSeconds(long ttlSeconds) {
			this.ttlSeconds = ttlSeconds;
		}
	}
}
