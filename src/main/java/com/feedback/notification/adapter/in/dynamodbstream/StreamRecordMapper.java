package com.feedback.notification.adapter.in.dynamodbstream;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.feedback.notification.application.usecase.NotifyAdminsCommand;
import com.feedback.notification.domain.model.Urgency;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

/** Turns a DynamoDB Streams record (NEW_IMAGE) into a command. */
public final class StreamRecordMapper {

    /**
     * Empty when the record is not an INSERT of a high-urgency evaluation. The event source
     * mapping already filters these in Terraform; this is a second line of defense.
     */
    public Optional<NotifyAdminsCommand> toCommand(DynamodbStreamRecord record) {
        if (!"INSERT".equals(record.getEventName()) || record.getDynamodb() == null) {
            return Optional.empty();
        }
        Map<String, AttributeValue> image = record.getDynamodb().getNewImage();
        if (image == null || Urgency.fromValue(string(image, "urgency", false)).orElse(null) != Urgency.HIGH) {
            return Optional.empty();
        }

        return Optional.of(new NotifyAdminsCommand(
                string(image, "evaluationId", true),
                string(image, "description", true),
                instant(string(image, "submittedAt", true))));
    }

    private static String string(Map<String, AttributeValue> image, String name, boolean required) {
        AttributeValue value = image.get(name);
        String text = value == null ? null : value.getS();
        if (required && (text == null || text.isBlank())) {
            throw new InvalidStreamRecordException("Stream record without " + name);
        }
        return text;
    }

    private static Instant instant(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new InvalidStreamRecordException("Stream record with invalid submittedAt");
        }
    }
}
