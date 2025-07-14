package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedApprovalRequestDTO;
import ai.shreds.shared.dtos.SharedModerationAssignmentDTO;
import ai.shreds.shared.value_objects.SharedModeratorAssignmentRequestParams;

/**
 * Input port for assigning moderators to approval requests.
 */
public interface ApplicationInputPortAssignModerator {

    /**
     * Assigns a moderator to an approval request.
     *
     * @param requestId the ID of the approval request
     * @param params the moderator assignment parameters
     * @return the updated approval request DTO
     */
    SharedApprovalRequestDTO assignModerator(String requestId, SharedModeratorAssignmentRequestParams params);

    /**
     * Processes a moderation assignment from the message queue.
     *
     * @param assignment the moderation assignment details
     * @return the updated approval request DTO
     */
    SharedApprovalRequestDTO processAssignment(SharedModerationAssignmentDTO assignment);
}