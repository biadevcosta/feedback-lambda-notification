package com.feedback.notification.adapter.in.dynamodbstream;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.feedback.notification.application.usecase.NotifyAdminsCommand;
import com.feedback.notification.application.usecase.NotifyAdminsResult;
import com.feedback.notification.application.usecase.NotifyAdminsUseCase;
import java.util.List;
import java.util.Optional;

/**
 * Handles a batch of stream records. Exceptions are not caught on purpose: the event source
 * mapping retries the batch and then sends it to the DLQ.
 */
public final class EvaluationStreamListener {

    private final NotifyAdminsUseCase useCase;
    private final StreamRecordMapper mapper;

    public EvaluationStreamListener(NotifyAdminsUseCase useCase, StreamRecordMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    public void handle(DynamodbEvent event, LambdaLogger logger) {
        List<DynamodbStreamRecord> records = event.getRecords() == null ? List.of() : event.getRecords();
        for (DynamodbStreamRecord record : records) {
            Optional<NotifyAdminsCommand> command = mapper.toCommand(record);
            if (command.isEmpty()) {
                logger.log("Skipped stream record " + record.getEventID() + ": not a high-urgency insert");
                continue;
            }
            NotifyAdminsResult result = useCase.execute(command.get());
            // LGPD: ids and counts only, never emails or the description
            logger.log("Evaluation " + command.get().evaluationId() + ": notified " + result.recipients() + " admins");
        }
    }
}
