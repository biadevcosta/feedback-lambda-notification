package com.feedback.notification.adapter.in.dynamodbstream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.feedback.notification.application.usecase.NotifyAdminsCommand;
import com.feedback.notification.application.usecase.NotifyAdminsResult;
import com.feedback.notification.application.usecase.NotifyAdminsUseCase;
import com.feedback.notification.fixtures.TestEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluationStreamListenerTest {

    @Mock
    private NotifyAdminsUseCase useCase;

    @Mock
    private LambdaLogger logger;

    @Captor
    private ArgumentCaptor<NotifyAdminsCommand> command;

    @Captor
    private ArgumentCaptor<String> logged;

    private EvaluationStreamListener listener;

    @BeforeEach
    void setUp() {
        listener = new EvaluationStreamListener(useCase, new StreamRecordMapper());
    }

    @Test
    void should_notifyOnlyHighUrgencyInserts_when_batchIsMixed() {
        // given
        when(useCase.execute(any(NotifyAdminsCommand.class))).thenReturn(new NotifyAdminsResult(2));

        // when
        listener.handle(TestEvents.dynamodbStream("stream-mixed.json"), logger);

        // then
        verify(useCase).execute(command.capture());
        assertThat(command.getValue().evaluationId()).isEqualTo("eval-high");
    }

    @Test
    void should_logOnlyIdsAndCounts() {
        when(useCase.execute(any(NotifyAdminsCommand.class))).thenReturn(new NotifyAdminsResult(2));

        listener.handle(TestEvents.dynamodbStream("stream-mixed.json"), logger);

        verify(logger, atLeastOnce()).log(logged.capture());
        assertThat(logged.getAllValues())
                .contains("Evaluation eval-high: notified 2 admins")
                .noneMatch(line -> line.contains("Too fast") || line.contains("Great class") || line.contains("@"));
    }

    @Test
    void should_propagateError_when_notificationFails() {
        // lets the event source mapping retry and then send the batch to the DLQ
        when(useCase.execute(any(NotifyAdminsCommand.class))).thenThrow(new IllegalStateException("SES unavailable"));

        assertThatThrownBy(() -> listener.handle(TestEvents.dynamodbStream("stream-insert-high.json"), logger))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_doNothing_when_eventHasNoRecords() {
        listener.handle(new DynamodbEvent(), logger);

        verifyNoInteractions(useCase);
    }
}
