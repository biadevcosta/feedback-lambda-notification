package com.feedback.notification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.feedback.notification.adapter.in.dynamodbstream.EvaluationStreamListener;
import com.feedback.notification.config.DependencyFactory;

/**
 * Lambda entry point (referenced by Terraform). Bootstrap only: the runtime creates one instance
 * per execution environment, so the dependencies built here are reused across invocations.
 */
public class NotificationHandler implements RequestHandler<DynamodbEvent, Void> {

    private final EvaluationStreamListener listener;

    public NotificationHandler() {
        this(DependencyFactory.fromEnvironment().evaluationStreamListener());
    }

    NotificationHandler(EvaluationStreamListener listener) {
        this.listener = listener;
    }

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        listener.handle(event, context.getLogger());
        return null;
    }
}
