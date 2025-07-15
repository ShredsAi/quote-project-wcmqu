package ai.shreds.application.events;

import ai.shreds.application.ports.ApplicationOutputPortSendNotification;
import ai.shreds.shared.dtos.SharedApprovalNotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ApplicationNotificationRequestEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNotificationRequestEventListener.class);
    private final ApplicationOutputPortSendNotification notificationSender;

    public ApplicationNotificationRequestEventListener(ApplicationOutputPortSendNotification notificationSender) {
        this.notificationSender = notificationSender;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(ApplicationNotificationRequestEvent event) {
        try {
            SharedApprovalNotificationDTO notification = new SharedApprovalNotificationDTO(
                    event.getType(),
                    event.getRecipientId(),
                    event.getMessage());
            notificationSender.sendNotification(notification);
            logger.debug("Notification sent after commit: {} for {}", event.getType(), event.getRecipientId());
        } catch (Exception e) {
            logger.error("Failed to send notification after commit for {} to {}: {}",
                         event.getType(), event.getRecipientId(), e.getMessage());
        }
    }
}