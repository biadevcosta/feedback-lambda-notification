package com.feedback.notification.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class UrgencyTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({"high, HIGH", "medium, MEDIUM", "low, LOW"})
    void should_findUrgency_when_valueIsKnown(String value, Urgency expected) {
        assertThat(Urgency.fromValue(value)).contains(expected);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "HIGH", "critical"})
    void should_returnEmpty_when_valueIsUnknown(String value) {
        assertThat(Urgency.fromValue(value)).isEmpty();
    }
}
