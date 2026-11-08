package com.feedback.notification.application.port.out;

import com.feedback.notification.domain.model.EmailMessage;

/** Delivers an email to one recipient. */
public interface EmailSender {

    void send(String recipient, EmailMessage message);
}
