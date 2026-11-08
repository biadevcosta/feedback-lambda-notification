package com.feedback.notification.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.feedback.notification.application.port.out.AdminDirectory;
import com.feedback.notification.application.port.out.EmailSender;
import com.feedback.notification.domain.model.EmailMessage;
import com.feedback.notification.domain.service.UrgencyEmailRenderer;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotifyAdminsUseCaseTest {

    private static final NotifyAdminsCommand COMMAND =
            new NotifyAdminsCommand("eval-1", "Too fast", Instant.parse("2026-11-09T01:30:00.000Z"));

    @Mock
    private AdminDirectory adminDirectory;

    @Mock
    private EmailSender emailSender;

    @Captor
    private ArgumentCaptor<EmailMessage> message;

    private NotifyAdminsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new NotifyAdminsUseCase(adminDirectory, emailSender, new UrgencyEmailRenderer());
    }

    @Test
    void should_emailEachAdminSeparately_when_thereAreAdmins() {
        // given
        when(adminDirectory.verifiedAdminEmails()).thenReturn(List.of("admin1@example.com", "admin2@example.com"));

        // when
        NotifyAdminsResult result = useCase.execute(COMMAND);

        // then
        assertThat(result.recipients()).isEqualTo(2);
        verify(emailSender).send(eq("admin1@example.com"), message.capture());
        verify(emailSender).send(eq("admin2@example.com"), any(EmailMessage.class));
        assertThat(message.getValue().textBody()).contains("Too fast");
    }

    @Test
    void should_sendNothing_when_thereAreNoAdmins() {
        when(adminDirectory.verifiedAdminEmails()).thenReturn(List.of());

        NotifyAdminsResult result = useCase.execute(COMMAND);

        assertThat(result.recipients()).isZero();
        verifyNoInteractions(emailSender);
    }

    @Test
    void should_propagateError_when_sendingFails() {
        // the stream retries and then keeps the event in the DLQ
        when(adminDirectory.verifiedAdminEmails()).thenReturn(List.of("admin1@example.com"));
        doThrow(new IllegalStateException("SES unavailable")).when(emailSender).send(any(), any());

        assertThatThrownBy(() -> useCase.execute(COMMAND)).isInstanceOf(IllegalStateException.class);
    }
}
