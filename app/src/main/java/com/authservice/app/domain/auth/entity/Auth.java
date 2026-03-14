package com.authservice.app.domain.auth.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.authservice.app.domain.user.entity.User;

@Entity
@Table(name = "auth")
@Getter
@NoArgsConstructor
public class Auth {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
	private UUID id;

	@OneToOne(optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Builder
	private Auth(User user, String passwordHash) {
		this.user = user;
		this.passwordHash = passwordHash;
	}
}
