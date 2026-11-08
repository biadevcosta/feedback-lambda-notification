package com.feedback.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.feedback.notification.adapter.in.dynamodbstream.EvaluationStreamListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationHandlerTest {

    @Mock
    private EvaluationStreamListener listener;

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    @Test
    void should_delegateToListener() {
        // given
        var event = new DynamodbEvent();
        when(context.getLogger()).thenReturn(logger);

        // when
        Void result = new NotificationHandler(listener).handleRequest(event, context);

        // then
        verify(listener).handle(event, logger);
        assertThat(result).isNull();
    }
}
