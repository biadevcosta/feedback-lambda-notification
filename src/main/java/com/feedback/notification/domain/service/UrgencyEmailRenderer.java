package com.feedback.notification.domain.service;

import com.feedback.notification.domain.model.CriticalEvaluation;
import com.feedback.notification.domain.model.EmailMessage;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Builds the urgency alert sent to admins. Pure: same evaluation, same email. */
public final class UrgencyEmailRenderer {

    static final String SUBJECT = "[Critical feedback] New evaluation with high urgency";

    // Brasília has no daylight saving time, so a fixed UTC-3 offset is enough
    private static final DateTimeFormatter SUBMITTED_AT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneOffset.ofHours(-3));

    public EmailMessage render(CriticalEvaluation evaluation) {
        String submittedAt = SUBMITTED_AT.format(evaluation.submittedAt()) + " (Brasília)";
        String urgency = evaluation.urgency().value();

        String text = """
                A new evaluation with high urgency was submitted.

                Description: %s
                Urgency: %s
                Submitted at: %s
                """.formatted(evaluation.description(), urgency, submittedAt);

        // The description is student-written text: escape it so it can't inject HTML into the email
        String html = """
                <html><body style="font-family: Arial, sans-serif">
                <h2 style="color: #b00020">New evaluation with high urgency</h2>
                <p><strong>Description:</strong> %s</p>
                <p><strong>Urgency:</strong> %s</p>
                <p><strong>Submitted at:</strong> %s</p>
                </body></html>
                """.formatted(escapeHtml(evaluation.description()), urgency, submittedAt);

        return new EmailMessage(SUBJECT, text, html);
    }

    static String escapeHtml(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            switch (c) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
