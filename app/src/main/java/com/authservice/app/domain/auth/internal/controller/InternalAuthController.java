package com.authservice.app.domain.auth.internal.controller;

import com.authservice.app.common.base.constant.SuccessCode;
import com.authservice.app.common.base.dto.GlobalResponse;
import com.authservice.app.domain.auth.internal.dto.InternalAuthRequest;
import com.authservice.app.domain.auth.internal.dto.InternalAuthResponse;
import com.authservice.app.domain.auth.internal.service.InternalAuthAccountService;
import com.authservice.app.domain.auth.userdirectory.config.InternalApiProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.authservice.app.common.swagger.constant.SwaggerTag;

@RestController
@RequestMapping("/internal/auth/accounts")
@RequiredArgsConstructor
@Tag(name = SwaggerTag.AUTH, description = "Internal auth account controller")
public class InternalAuthController {

	private final InternalAuthAccountService internalAuthAccountService;
	private final InternalApiProperties internalApiProperties;

	@PostMapping
	public GlobalResponse<InternalAuthResponse.AccountResponse> createAccount(
		@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
		@Valid @RequestBody InternalAuthRequest.CreateAccountRequest request
	) {
		internalApiProperties.validateAuthorizationHeader(authorization);
		return GlobalResponse.ok(
			SuccessCode.SUCCESS,
			internalAuthAccountService.createAccount(request)
		);
	}

	@DeleteMapping("/{userId}")
	public GlobalResponse<Void> deleteAccount(
		@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
		@PathVariable UUID userId
	) {
		internalApiProperties.validateAuthorizationHeader(authorization);
		internalAuthAccountService.deleteAccount(userId);
		return GlobalResponse.ok();
	}
}
