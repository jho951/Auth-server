package com.authservice.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.authservice.app.domain.auth.sso.controller.SsoController;
import com.authservice.app.domain.auth.sso.dto.SsoResponse;
import com.authservice.app.domain.auth.sso.model.SsoPrincipal;
import com.authservice.app.domain.auth.sso.service.SsoAuthService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class SsoControllerFlowTest {

	@Mock
	private SsoAuthService ssoAuthService;

	private SsoController ssoController;

	@BeforeEach
	void setUp() {
		ssoController = new SsoController(ssoAuthService);
	}

	@Test
	void logoutDelegatesToSsoAuthService() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		when(ssoAuthService.logout(request)).thenReturn(ResponseEntity.noContent().build());

		ResponseEntity<Void> response = ssoController.logout(request);

		assertThat(response.getStatusCode().value()).isEqualTo(204);
		verify(ssoAuthService).logout(request);
	}

	@Test
	void sessionDelegatesToSsoAuthService() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		SsoResponse.InternalSessionValidationResponse validation =
			new SsoResponse.InternalSessionValidationResponse(true, "user-1", "USER", "ACTIVE", "session-1");
		when(ssoAuthService.validateInternalSession(request)).thenReturn(ResponseEntity.ok(validation));

		ResponseEntity<SsoResponse.InternalSessionValidationResponse> response = ssoController.session(request);

		assertThat(response.getBody()).isSameAs(validation);
		verify(ssoAuthService).validateInternalSession(request);
	}

	@Test
	void internalSessionValidateDelegatesToSsoAuthService() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		SsoResponse.InternalSessionValidationResponse validation =
			new SsoResponse.InternalSessionValidationResponse(false, "", "", "", "");
		when(ssoAuthService.validateInternalSession(request)).thenReturn(ResponseEntity.status(401).body(validation));

		ResponseEntity<SsoResponse.InternalSessionValidationResponse> response = ssoController.validateSession(request);

		assertThat(response.getStatusCode().value()).isEqualTo(401);
		assertThat(response.getBody()).isSameAs(validation);
		verify(ssoAuthService).validateInternalSession(request);
	}

	@Test
	void meReturnsCurrentUserSummary() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		SsoPrincipal principal = new SsoPrincipal(
			"user-1",
			"user@example.com",
			"User",
			"https://example.com/avatar.png",
			List.of("USER"),
			"ACTIVE"
		);
		when(ssoAuthService.getCurrentUser(request, "editor")).thenReturn(principal);

		SsoResponse.MeResponse response = ssoController.me(request, "editor");

		assertThat(response.getId()).isEqualTo("user-1");
		assertThat(response.getEmail()).isEqualTo("user@example.com");
		assertThat(response.getRoles()).containsExactly("USER");
		verify(ssoAuthService).getCurrentUser(request, "editor");
	}
}
