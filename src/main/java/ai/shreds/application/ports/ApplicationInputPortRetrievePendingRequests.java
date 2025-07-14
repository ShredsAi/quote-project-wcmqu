package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedApprovalRequestDTO;
import ai.shreds.shared.value_objects.SharedPendingRequestsQueryParams;

import java.util.List;

/**
 * Input port for retrieving pending approval requests.
 */
public interface ApplicationInputPortRetrievePendingRequests {

    /**
     * Retrieves a list of pending approval requests based on query criteria.
     *
     * @param queryParams the query parameters for filtering requests
     * @return list of pending approval request DTOs
     */
    List<SharedApprovalRequestDTO> retrievePendingRequests(SharedPendingRequestsQueryParams queryParams);
}