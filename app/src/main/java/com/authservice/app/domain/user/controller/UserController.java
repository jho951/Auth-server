package com.authservice.app.domain.user.controller;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authservice.app.domain.user.dto.UserRequest;
import com.authservice.app.domain.user.dto.UserResponse;
import com.authservice.app.domain.user.service.UserService;
import com.authservice.app.common.swagger.constant.SwaggerTag;
import com.authservice.app.common.base.dto.GlobalResponse;
import com.authservice.app.common.base.constant.SuccessCode;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Tag(name = SwaggerTag.USER, description = "This is a user controller")
public class UserController {
	private final UserService userService;

	@PostMapping("/signup")
	public GlobalResponse<UserResponse.UserCreateResponse> cancel(@Valid @RequestBody UserRequest.UserCreateRequest dto) {
		UserResponse.UserCreateResponse response = userService.create(dto);
		return GlobalResponse.ok(SuccessCode.SUCCESS, response);
	}
}
