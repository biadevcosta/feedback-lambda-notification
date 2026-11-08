package com.feedback.notification.adapter.in.dynamodbstream;

/** A stream record is missing data required by the contract; it should end up in the DLQ. */
public class InvalidStreamRecordException extends RuntimeException {

    public InvalidStreamRecordException(String message) {
        super(message);
    }
}
