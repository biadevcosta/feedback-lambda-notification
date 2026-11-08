package com.feedback.notification.application.port.out;

import java.util.List;

/** Finds who must be alerted. */
public interface AdminDirectory {

    /** Emails of the admins whose address is verified; empty if there is none. */
    List<String> verifiedAdminEmails();
}
