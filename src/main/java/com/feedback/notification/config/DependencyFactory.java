package com.feedback.notification.config;

import com.feedback.notification.adapter.in.dynamodbstream.EvaluationStreamListener;
import com.feedback.notification.adapter.in.dynamodbstream.StreamRecordMapper;
import com.feedback.notification.adapter.out.cognito.CognitoAdminDirectory;
import com.feedback.notification.adapter.out.ses.SesEmailSender;
import com.feedback.notification.application.usecase.NotifyAdminsUseCase;
import com.feedback.notification.domain.service.UrgencyEmailRenderer;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.ses.SesClient;

/** Composition root: builds the object graph once per execution environment. */
public final class DependencyFactory {

    private final EnvironmentConfig config;
    private final CognitoIdentityProviderClient cognito;
    private final SesClient ses;

    private DependencyFactory(EnvironmentConfig config) {
        this.config = config;
        // Region comes from AWS_REGION, set by Lambda
        this.cognito = CognitoIdentityProviderClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
        this.ses = SesClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
    }

    public static DependencyFactory fromEnvironment() {
        return new DependencyFactory(EnvironmentConfig.fromEnvironment());
    }

    public EvaluationStreamListener evaluationStreamListener() {
        var useCase = new NotifyAdminsUseCase(
                new CognitoAdminDirectory(cognito, config.userPoolId(), config.adminGroup()),
                new SesEmailSender(ses, config.sesSender()),
                new UrgencyEmailRenderer());
        return new EvaluationStreamListener(useCase, new StreamRecordMapper());
    }
}
