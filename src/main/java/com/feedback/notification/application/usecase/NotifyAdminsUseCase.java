package com.feedback.notification.application.usecase;

import com.feedback.notification.application.port.out.AdminDirectory;
import com.feedback.notification.application.port.out.EmailSender;
import com.feedback.notification.domain.model.CriticalEvaluation;
import com.feedback.notification.domain.model.EmailMessage;
import com.feedback.notification.domain.service.UrgencyEmailRenderer;
import java.util.List;

/** Emails every verified admin about a high-urgency evaluation. */
public final class NotifyAdminsUseCase {

    private final AdminDirectory adminDirectory;
    private final EmailSender emailSender;
    private final UrgencyEmailRenderer renderer;

    public NotifyAdminsUseCase(AdminDirectory adminDirectory, EmailSender emailSender, UrgencyEmailRenderer renderer) {
        this.adminDirectory = adminDirectory;
        this.emailSender = emailSender;
        this.renderer = renderer;
    }

    /**
     * Sends one email per admin, so admins don't see each other's address. Failures propagate,
     * letting the stream retry and then the DLQ keep the event.
     */
    public NotifyAdminsResult execute(NotifyAdminsCommand command) {
        List<String> recipients = adminDirectory.verifiedAdminEmails();
        if (recipients.isEmpty()) {
            return new NotifyAdminsResult(0);
        }

        EmailMessage message = renderer.render(
                new CriticalEvaluation(command.evaluationId(), command.description(), command.submittedAt()));
        recipients.forEach(recipient -> emailSender.send(recipient, message));
        return new NotifyAdminsResult(recipients.size());
    }
}
