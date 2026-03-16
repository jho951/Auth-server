package com.authservice.app.domain.auth.userdirectory.model;

public record OAuth2ProvisionCommand(
	String provider,
	String providerUserId,
	String email,
	String name,
	String avatarUrl
) {
}
