package com.authservice.app.domain.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.auditlog.api.AuditEvent;
import com.auditlog.api.AuditLogger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuthAuditLogServiceTest {

	@Test
	void successAuditDoesNotForceFailureReason() {
		AuditLogger auditLogger = mock(AuditLogger.class);
		AuthAuditLogService service = new AuthAuditLogService(auditLogger);

		service.logSsoLoginSuccess("user-1", "github");

		ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
		verify(auditLogger).log(captor.capture());
		AuditEvent event = captor.getValue();

		assertThat(event.getResult().name()).isEqualTo("SUCCESS");
		assertThat(event.getReason()).isNull();
	}

	@Test
	void failureAuditKeepsDefaultFailureReason() {
		AuditLogger auditLogger = mock(AuditLogger.class);
		AuthAuditLogService service = new AuthAuditLogService(auditLogger);

		service.logPasswordLoginFailure("user@example.com", null);

		ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
		verify(auditLogger).log(captor.capture());
		AuditEvent event = captor.getValue();

		assertThat(event.getResult().name()).isEqualTo("FAILURE");
		assertThat(event.getReason()).isEqualTo("AUTH_FAILURE");
	}
}
