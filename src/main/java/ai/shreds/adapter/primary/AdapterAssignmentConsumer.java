package ai.shreds.adapter.primary;

import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import ai.shreds.application.ports.ApplicationInputPortAssignModerator;
import ai.shreds.shared.dtos.SharedModerationAssignmentDTO;

/**
 * Adapter consuming moderation assignment messages from RabbitMQ and forwarding to the application core.
 */
@Component
public class AdapterAssignmentConsumer {

    private final ApplicationInputPortAssignModerator assignModeratorPort;

    public AdapterAssignmentConsumer(ApplicationInputPortAssignModerator assignModeratorPort) {
        this.assignModeratorPort = assignModeratorPort;
    }

    @RabbitListener(queues = "${approval.queues.assignments}")
    public void consumeAssignment(SharedModerationAssignmentDTO assignment) {
        assignModeratorPort.processAssignment(assignment);
    }
}
