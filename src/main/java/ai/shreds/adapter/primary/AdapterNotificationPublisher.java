package ai.shreds.adapter.primary;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ai.shreds.application.ports.ApplicationOutputPortSendNotification;
import ai.shreds.shared.dtos.SharedApprovalNotificationDTO;

/**
 * Adapter sending approval workflow notifications to RabbitMQ
 */
@Component
public class AdapterNotificationPublisher implements ApplicationOutputPortSendNotification {

    private final RabbitTemplate rabbitTemplate;
    private final String notificationQueue;

    public AdapterNotificationPublisher(RabbitTemplate rabbitTemplate,
                                        @Value("${approval.queues.notifications}") String notificationQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.notificationQueue = notificationQueue;
    }

    @Override
    public void sendNotification(SharedApprovalNotificationDTO notification) {
        rabbitTemplate.convertAndSend(notificationQueue, notification);
    }
}
