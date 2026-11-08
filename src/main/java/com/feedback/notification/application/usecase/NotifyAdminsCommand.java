package com.feedback.notification.application.usecase;

import java.time.Instant;

/** Input of NotifyAdminsUseCase: a high-urgency evaluation read from the stream. */
public record NotifyAdminsCommand(String evaluationId, String description, Instant submittedAt) {
}
