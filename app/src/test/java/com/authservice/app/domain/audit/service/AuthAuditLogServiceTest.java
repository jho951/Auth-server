package com.authservice.app.domain.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.jho951.platform.governance.api.AuditEntry;
import io.github.jho951.platform.governance.api.GovernanceAuditRecorder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuthAuditLogServiceTest {

    @Test
    void logsPasswordLoginSuccess() {
        GovernanceAuditRecorder auditRecorder = mock(GovernanceAuditRecorder.class);
        AuthAuditLogService service = new AuthAuditLogService(auditRecorder);

        service.logPasswordLoginSuccess("user-1");

        ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(auditRecorder).record(captor.capture());
        assertThat(captor.getValue().category()).isEqualTo("auth");
        assertThat(captor.getValue().message()).isEqualTo("AUTH_LOGIN_PASSWORD");
        assertThat(captor.getValue().attributes()).containsEntry("result", "SUCCESS");
    }

    @Test
    void logsPasswordLoginFailure() {
        GovernanceAuditRecorder auditRecorder = mock(GovernanceAuditRecorder.class);
        AuthAuditLogService service = new AuthAuditLogService(auditRecorder);

        service.logPasswordLoginFailure("user-1", "INVALID_CREDENTIALS");

        ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(auditRecorder).record(captor.capture());
        assertThat(captor.getValue().attributes())
            .containsEntry("result", "FAILURE")
            .containsEntry("reason", "INVALID_CREDENTIALS");
    }
}
