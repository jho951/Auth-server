package com.authservice.app.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.authservice.app.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

	@Query("SELECT s FROM User s WHERE s.email = :email")
	Optional<User> findByEmail(String email);

}
