package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedApprovalQueueDTO;
import ai.shreds.shared.value_objects.SharedQueuesQueryParams;
import java.util.List;

/**
 * Application input port for retrieving approval queues.
 */
public interface ApplicationInputPortRetrieveQueues {

    /**
     * Retrieves a list of approval queues based on provided filters.
     *
     * @param queryParams filters such as active flag and moderator ID
     * @return list of approval queue DTOs
     */
    List<SharedApprovalQueueDTO> retrieveQueues(SharedQueuesQueryParams queryParams);
}
