package com.authservice.app.domain.user.service;

import com.authservice.app.domain.user.dto.UserRequest;
import com.authservice.app.domain.user.dto.UserResponse;

public interface UserService {
	UserResponse.UserCreateResponse create(UserRequest.UserCreateRequest dto);
}
