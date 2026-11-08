package com.feedback.notification.adapter.in.dynamodbstream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.feedback.notification.application.usecase.NotifyAdminsCommand;
import com.feedback.notification.fixtures.TestEvents;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StreamRecordMapperTest {

    private final StreamRecordMapper mapper = new StreamRecordMapper();

    @Test
    void should_buildCommand_when_recordIsHighUrgencyInsert() {
        assertThat(mapper.toCommand(highUrgencyInsert())).contains(new NotifyAdminsCommand(
                "eval-1", "The class was <b>confusing</b>", Instant.parse("2026-11-09T01:30:00.000Z")));
    }

    @Test
    void should_skip_when_recordIsNotAHighUrgencyInsert() {
        List<DynamodbStreamRecord> records = TestEvents.dynamodbStream("stream-mixed.json").getRecords();

        // INSERT high, INSERT low, MODIFY high, REMOVE
        assertThat(records).extracting(mapper::toCommand)
                .extracting(command -> command.isPresent())
                .containsExactly(true, false, false, false);
    }

    @Test
    void should_skip_when_urgencyIsMissing() {
        DynamodbStreamRecord record = highUrgencyInsert();
        record.getDynamodb().getNewImage().remove("urgency");

        assertThat(mapper.toCommand(record)).isEmpty();
    }

    @ParameterizedTest(name = "without {0}")
    @ValueSource(strings = {"evaluationId", "description", "submittedAt"})
    void should_rejectRecord_when_requiredAttributeIsMissing(String attribute) {
        DynamodbStreamRecord record = highUrgencyInsert();
        record.getDynamodb().getNewImage().remove(attribute);

        assertThatThrownBy(() -> mapper.toCommand(record))
                .isInstanceOf(InvalidStreamRecordException.class)
                .hasMessage("Stream record without " + attribute);
    }

    @Test
    void should_rejectRecord_when_submittedAtIsNotIso8601() {
        DynamodbStreamRecord record = highUrgencyInsert();
        record.getDynamodb().getNewImage().put("submittedAt", new AttributeValue().withS("09/11/2026"));

        assertThatThrownBy(() -> mapper.toCommand(record)).isInstanceOf(InvalidStreamRecordException.class);
    }

    private static DynamodbStreamRecord highUrgencyInsert() {
        return TestEvents.dynamodbStream("stream-insert-high.json").getRecords().get(0);
    }
}
