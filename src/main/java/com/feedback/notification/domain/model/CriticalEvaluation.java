package com.feedback.notification.domain.model;

import java.time.Instant;
import java.util.Objects;

/** A newly submitted evaluation with high urgency, the only kind admins are alerted about. */
public record CriticalEvaluation(String evaluationId, String description, Instant submittedAt) {

    public CriticalEvaluation {
        Objects.requireNonNull(evaluationId, "evaluationId");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(submittedAt, "submittedAt");
    }

    public Urgency urgency() {
        return Urgency.HIGH;
    }
}
