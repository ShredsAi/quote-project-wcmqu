package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedApprovalNotificationDTO;

/**
 * Output port for sending approval workflow notifications.
 */
public interface ApplicationOutputPortSendNotification {

    /**
     * Sends a notification about approval workflow events.
     *
     * @param notification the notification details including type, recipient, and message
     */
    void sendNotification(SharedApprovalNotificationDTO notification);
}