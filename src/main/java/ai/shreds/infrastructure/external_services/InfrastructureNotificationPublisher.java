package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationOutputPortSendNotification;
import ai.shreds.infrastructure.exceptions.InfrastructureMessagingException;
import ai.shreds.shared.dtos.SharedApprovalNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfrastructureNotificationPublisher implements ApplicationOutputPortSendNotification {

    private final RabbitTemplate rabbitTemplate;
    
    @Value("${approval.queues.notifications}")
    private String notificationQueueName;

    @Override
    public void sendNotification(SharedApprovalNotificationDTO notification) {
        try {
            log.debug("Sending notification of type: {} to recipient: {}", 
                notification.getType(), notification.getRecipientId());
            
            // Validate the notification
            validateNotification(notification);
            
            // Send the notification to the RabbitMQ queue
            rabbitTemplate.convertAndSend(notificationQueueName, notification);
            
            log.info("Successfully sent notification of type: {} to recipient: {}", 
                notification.getType(), notification.getRecipientId());
        } catch (Exception e) {
            log.error("Error sending notification of type: {} to recipient: {}", 
                notification.getType(), notification.getRecipientId(), e);
            throw new InfrastructureMessagingException("Failed to send notification", e);
        }
    }

    /**
     * Sends a notification with routing key for more specific delivery.
     * 
     * @param notification the notification to send
     * @param routingKey the routing key for message routing
     */
    public void sendNotificationWithRoutingKey(SharedApprovalNotificationDTO notification, String routingKey) {
        try {
            log.debug("Sending notification of type: {} to recipient: {} with routing key: {}", 
                notification.getType(), notification.getRecipientId(), routingKey);
            
            // Validate the notification
            validateNotification(notification);
            
            // Send the notification to the RabbitMQ queue with routing key
            rabbitTemplate.convertAndSend(notificationQueueName, routingKey, notification);
            
            log.info("Successfully sent notification of type: {} to recipient: {} with routing key: {}", 
                notification.getType(), notification.getRecipientId(), routingKey);
        } catch (Exception e) {
            log.error("Error sending notification of type: {} to recipient: {} with routing key: {}", 
                notification.getType(), notification.getRecipientId(), routingKey, e);
            throw new InfrastructureMessagingException("Failed to send notification with routing key", e);
        }
    }

    /**
     * Sends a high-priority notification.
     * 
     * @param notification the notification to send
     */
    public void sendHighPriorityNotification(SharedApprovalNotificationDTO notification) {
        try {
            log.debug("Sending high-priority notification of type: {} to recipient: {}", 
                notification.getType(), notification.getRecipientId());
            
            // Validate the notification
            validateNotification(notification);
            
            // Send with high priority by setting message properties
            rabbitTemplate.convertAndSend(notificationQueueName, notification, message -> {
                message.getMessageProperties().setPriority(9); // High priority
                message.getMessageProperties().setExpiration("300000"); // 5 minutes expiration
                return message;
            });
            
            log.info("Successfully sent high-priority notification of type: {} to recipient: {}", 
                notification.getType(), notification.getRecipientId());
        } catch (Exception e) {
            log.error("Error sending high-priority notification of type: {} to recipient: {}", 
                notification.getType(), notification.getRecipientId(), e);
            throw new InfrastructureMessagingException("Failed to send high-priority notification", e);
        }
    }

    /**
     * Sends a batch of notifications.
     * 
     * @param notifications the list of notifications to send
     */
    public void sendBatchNotifications(java.util.List<SharedApprovalNotificationDTO> notifications) {
        try {
            log.debug("Sending batch of {} notifications", notifications.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (SharedApprovalNotificationDTO notification : notifications) {
                try {
                    sendNotification(notification);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to send notification in batch: {}", notification.getType(), e);
                    failureCount++;
                }
            }
            
            log.info("Batch notification sending completed. Success: {}, Failures: {}", 
                successCount, failureCount);
        } catch (Exception e) {
            log.error("Error sending batch notifications", e);
            throw new InfrastructureMessagingException("Failed to send batch notifications", e);
        }
    }

    /**
     * Validates the notification before sending.
     * 
     * @param notification the notification to validate
     * @throws IllegalArgumentException if the notification is invalid
     */
    private void validateNotification(SharedApprovalNotificationDTO notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }
        
        if (notification.getType() == null || notification.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Notification type cannot be null or empty");
        }
        
        if (notification.getRecipientId() == null || notification.getRecipientId().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient ID cannot be null or empty");
        }
        
        if (notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Notification message cannot be null or empty");
        }
    }

    /**
     * Checks if the notification service is available.
     * 
     * @return true if the service is available, false otherwise
     */
    public boolean isServiceAvailable() {
        try {
            // Try to send a test message with a short timeout
            rabbitTemplate.convertAndSend("test.queue", "ping");
            return true;
        } catch (Exception e) {
            log.warn("Notification service is not available", e);
            return false;
        }
    }
}