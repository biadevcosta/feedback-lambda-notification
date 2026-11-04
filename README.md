# feedback-lambda-notification

**Notification** Lambda for the Class Feedback Platform (Tech Challenge phase 4).

Triggered by **DynamoDB Streams** when an evaluation with `high` urgency is inserted. Looks up the `admins` group's emails in Cognito and sends the alert through SES.

- **AWS function:** `feedback-notification` · **Runtime:** Java 21 (Maven) · **Region:** `sa-east-1`
- **Trigger:** DynamoDB Streams (filter: `INSERT` + `urgency = high`), with an SQS queue as the failure destination
- **Permissions:** `cognito-idp:ListUsersInGroup` and `ses:SendEmail`

## Email content

Evaluation description, urgency and submission date.

## Structure (in progress)

```
pom.xml
src/main/java/com/feedback/notification/NotificationHandler.java
src/test/java/...              # JUnit5 + Mockito mocking Cognito and SES
.github/workflows/ci-cd.yml
```

## Related repositories

`feedback-infra` · `feedback-lambda-ingestion` · `feedback-lambda-report`

## Status

Track progress in [PLANO.md](PLANO.md).
