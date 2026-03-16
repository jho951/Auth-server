package com.authservice.app.domain.auth.userdirectory.model;

import java.util.UUID;

public record UserAccountProfile(
	UUID userId,
	String email,
	String name,
	String role,
	String status,
	String avatarUrl
) {
}
