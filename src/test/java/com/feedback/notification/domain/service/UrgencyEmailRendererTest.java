package com.feedback.notification.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.feedback.notification.domain.model.CriticalEvaluation;
import com.feedback.notification.domain.model.EmailMessage;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UrgencyEmailRendererTest {

    // 01:30 UTC on Nov 9 is 22:30 on Nov 8 in Brasília
    private static final Instant SUBMITTED_AT = Instant.parse("2026-11-09T01:30:00.000Z");

    private final UrgencyEmailRenderer renderer = new UrgencyEmailRenderer();

    @Test
    void should_useCriticalFeedbackSubject() {
        EmailMessage email = renderer.render(evaluation("Too fast"));

        assertThat(email.subject()).startsWith("[Critical feedback]");
    }

    @Test
    void should_includeDescriptionUrgencyAndBrasiliaTime_when_renderingText() {
        EmailMessage email = renderer.render(evaluation("Too fast"));

        assertThat(email.textBody())
                .contains("Description: Too fast")
                .contains("Urgency: high")
                .contains("Submitted at: 08/11/2026 22:30 (Brasília)");
    }

    @Test
    void should_includeTheSameDataInHtml() {
        EmailMessage email = renderer.render(evaluation("Too fast"));

        assertThat(email.htmlBody())
                .contains("Too fast")
                .contains("high")
                .contains("08/11/2026 22:30 (Brasília)");
    }

    @Test
    void should_escapeDescription_when_itContainsHtml() {
        EmailMessage email = renderer.render(evaluation("<script>alert('x')</script> & more"));

        assertThat(email.htmlBody())
                .doesNotContain("<script>")
                .contains("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt; &amp; more");
        assertThat(email.textBody()).contains("<script>alert('x')</script> & more");
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(delimiter = '|', value = {
        "a&b | a&amp;b",
        "\"quoted\" | &quot;quoted&quot;",
        "plain text | plain text"
    })
    void should_escapeHtmlSpecialCharacters(String input, String expected) {
        assertThat(UrgencyEmailRenderer.escapeHtml(input)).isEqualTo(expected);
    }

    private static CriticalEvaluation evaluation(String description) {
        return new CriticalEvaluation("eval-1", description, SUBMITTED_AT);
    }
}
