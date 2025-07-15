package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainApprovalAuditLogEntity;
import ai.shreds.domain.ports.DomainOutputPortAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain service for maintaining comprehensive audit logging for all approval activities.
 * This service handles audit trail creation, retrieval, and reporting.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DomainAuditTrailService {

    private final DomainOutputPortAuditLogRepository auditLogRepository;

    private static final DateTimeFormatter AUDIT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Records an action performed on an approval request.
     *
     * @param requestId the ID of the approval request
     * @param action the action that was performed
     * @param performedById the ID of the user who performed the action
     * @param oldValue the previous value (if applicable)
     * @param newValue the new value (if applicable)
     * @return the created audit log entity
     * @throws IllegalArgumentException if required parameters are null or invalid
     */
    public DomainApprovalAuditLogEntity recordAction(
            String requestId,
            String action,
            String performedById,
            String oldValue,
            String newValue) {

        validateRecordActionParameters(requestId, action, performedById);

        DomainApprovalAuditLogEntity auditLog = DomainApprovalAuditLogEntity.builder()
                .auditId(UUID.randomUUID().toString())
                .approvalRequestId(requestId)
                .action(action)
                .performedById(performedById)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(LocalDateTime.now())
                .build();

        // Initialize the audit log
        auditLog.initializeAuditLog();

        // Validate the audit log
        auditLog.validate();

        // Save the audit log
        DomainApprovalAuditLogEntity savedAuditLog = auditLogRepository.save(auditLog);

        log.debug("Recorded audit action '{}' for request {} by user {}", action, requestId, performedById);

        return savedAuditLog;
    }

    /**
     * Records an action with IP address and user agent information.
     *
     * @param requestId the ID of the approval request
     * @param action the action that was performed
     * @param performedById the ID of the user who performed the action
     * @param oldValue the previous value (if applicable)
     * @param newValue the new value (if applicable)
     * @param ipAddress the IP address from which the action was performed
     * @param userAgent the user agent of the client
     * @return the created audit log entity
     * @throws IllegalArgumentException if required parameters are null or invalid
     */
    public DomainApprovalAuditLogEntity recordActionWithContext(
            String requestId,
            String action,
            String performedById,
            String oldValue,
            String newValue,
            String ipAddress,
            String userAgent) {

        validateRecordActionParameters(requestId, action, performedById);

        DomainApprovalAuditLogEntity auditLog = DomainApprovalAuditLogEntity.builder()
                .auditId(UUID.randomUUID().toString())
                .approvalRequestId(requestId)
                .action(action)
                .performedById(performedById)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        // Initialize the audit log
        auditLog.initializeAuditLog();

        // Validate the audit log
        auditLog.validate();

        // Save the audit log
        DomainApprovalAuditLogEntity savedAuditLog = auditLogRepository.save(auditLog);

        log.debug("Recorded audit action '{}' for request {} by user {} from IP {}", action, requestId, performedById, ipAddress);

        return savedAuditLog;
    }

    /**
     * Retrieves the complete audit trail for a specific approval request.
     *
     * @param requestId the ID of the approval request
     * @return a list of audit log entities ordered by timestamp
     * @throws IllegalArgumentException if requestId is null or empty
     */
    public List<DomainApprovalAuditLogEntity> retrieveAuditTrail(String requestId) {
        validateRequestId(requestId);

        List<DomainApprovalAuditLogEntity> auditTrail = auditLogRepository.findByRequestIdOrderByTimestamp(requestId);

        log.debug("Retrieved audit trail for request {} with {} entries", requestId, auditTrail.size());

        return auditTrail;
    }

    /**
     * Retrieves audit logs for a specific action type.
     *
     * @param action the action type to filter by
     * @return a list of audit log entities for the specified action
     * @throws IllegalArgumentException if action is null or empty
     */
    public List<DomainApprovalAuditLogEntity> retrieveAuditLogsByAction(String action) {
        validateAction(action);

        List<DomainApprovalAuditLogEntity> auditLogs = auditLogRepository.findByAction(action);

        log.debug("Retrieved {} audit logs for action '{}'", auditLogs.size(), action);

        return auditLogs;
    }

    /**
     * Retrieves audit logs for a specific user.
     *
     * @param performedById the ID of the user
     * @return a list of audit log entities for the specified user
     * @throws IllegalArgumentException if performedById is null or empty
     */
    public List<DomainApprovalAuditLogEntity> retrieveAuditLogsByUser(String performedById) {
        validatePerformedById(performedById);

        List<DomainApprovalAuditLogEntity> auditLogs = auditLogRepository.findByPerformedById(performedById);

        log.debug("Retrieved {} audit logs for user {}", auditLogs.size(), performedById);

        return auditLogs;
    }

    /**
     * Retrieves audit logs within a specific time range.
     *
     * @param startDate the start of the time range
     * @param endDate the end of the time range
     * @return a list of audit log entities within the time range
     * @throws IllegalArgumentException if dates are null or invalid
     */
    public List<DomainApprovalAuditLogEntity> retrieveAuditLogsByTimeRange(LocalDateTime startDate, LocalDateTime endDate) {
        validateTimeRange(startDate, endDate);

        List<DomainApprovalAuditLogEntity> auditLogs = auditLogRepository.findByTimestampBetween(startDate, endDate);

        log.debug("Retrieved {} audit logs between {} and {}", auditLogs.size(), startDate, endDate);

        return auditLogs;
    }

    /**
     * Generates a comprehensive audit report for a specific approval request.
     *
     * @param requestId the ID of the approval request
     * @param startDate the start date for the report (optional)
     * @param endDate the end date for the report (optional)
     * @return a formatted audit report as a string
     * @throws IllegalArgumentException if requestId is null or empty
     */
    public String generateAuditReport(String requestId, LocalDateTime startDate, LocalDateTime endDate) {
        validateRequestId(requestId);

        List<DomainApprovalAuditLogEntity> auditLogs;

        if (startDate != null && endDate != null) {
            validateTimeRange(startDate, endDate);
            auditLogs = auditLogRepository.findByRequestIdOrderByTimestamp(requestId)
                    .stream()
                    .filter(log -> !log.getTimestamp().isBefore(startDate) && !log.getTimestamp().isAfter(endDate))
                    .collect(Collectors.toList());
        } else {
            auditLogs = auditLogRepository.findByRequestIdOrderByTimestamp(requestId);
        }

        StringBuilder report = new StringBuilder();
        report.append("=== AUDIT REPORT FOR REQUEST ").append(requestId).append(" ===\n\n");

        if (startDate != null && endDate != null) {
            report.append("Report Period: ").append(startDate.format(AUDIT_DATE_FORMAT))
                    .append(" to ").append(endDate.format(AUDIT_DATE_FORMAT)).append("\n\n");
        }

        report.append("Total Audit Entries: ").append(auditLogs.size()).append("\n\n");

        if (auditLogs.isEmpty()) {
            report.append("No audit entries found for the specified criteria.\n");
            return report.toString();
        }

        // Group actions by type for summary
        Map<String, Long> actionCounts = auditLogs.stream()
                .collect(Collectors.groupingBy(
                        DomainApprovalAuditLogEntity::getAction,
                        Collectors.counting()
                ));

        report.append("=== ACTION SUMMARY ===\n");
        actionCounts.forEach((action, count) ->
                report.append("- ").append(action).append(": ").append(count).append("\n"));
        report.append("\n");

        // Detailed audit trail
        report.append("=== DETAILED AUDIT TRAIL ===\n");
        for (DomainApprovalAuditLogEntity auditLog : auditLogs) {
            report.append("[").append(auditLog.getTimestamp().format(AUDIT_DATE_FORMAT)).append("] ")
                    .append(auditLog.getAction()).append(" by ").append(auditLog.getPerformedById());

            if (auditLog.getOldValue() != null || auditLog.getNewValue() != null) {
                report.append(" (")
                        .append(auditLog.getOldValue() != null ? auditLog.getOldValue() : "null")
                        .append(" -> ")
                        .append(auditLog.getNewValue() != null ? auditLog.getNewValue() : "null")
                        .append(")");
            }

            if (auditLog.getIpAddress() != null) {
                report.append(" from IP: ").append(auditLog.getIpAddress());
            }

            report.append("\n");
        }

        // Timeline analysis
        report.append("\n=== TIMELINE ANALYSIS ===\n");
        if (auditLogs.size() > 1) {
            LocalDateTime firstAction = auditLogs.get(0).getTimestamp();
            LocalDateTime lastAction = auditLogs.get(auditLogs.size() - 1).getTimestamp();

            report.append("First Action: ").append(firstAction.format(AUDIT_DATE_FORMAT)).append("\n")
                    .append("Last Action: ").append(lastAction.format(AUDIT_DATE_FORMAT)).append("\n")
                    .append("Total Duration: ").append(calculateDuration(firstAction, lastAction)).append("\n");
        }

        report.append("\n=== END OF REPORT ===\n");

        log.info("Generated audit report for request {} with {} entries", requestId, auditLogs.size());

        return report.toString();
    }

    /**
     * Gets the most recent audit log entry for a specific approval request.
     *
     * @param requestId the ID of the approval request
     * @return the most recent audit log entry if found
     * @throws IllegalArgumentException if requestId is null or empty
     */
    public Optional<DomainApprovalAuditLogEntity> getMostRecentAuditEntry(String requestId) {
        validateRequestId(requestId);

        Optional<DomainApprovalAuditLogEntity> mostRecentEntry = auditLogRepository.findMostRecentByRequestId(requestId);

        log.debug("Retrieved most recent audit entry for request {}: {}", requestId, mostRecentEntry.isPresent() ? "found" : "not found");

        return mostRecentEntry;
    }

    /**
     * Counts the total number of audit entries for a specific request.
     *
     * @param requestId the ID of the approval request
     * @return the number of audit entries for the request
     * @throws IllegalArgumentException if requestId is null or empty
     */
    public long countAuditEntriesForRequest(String requestId) {
        validateRequestId(requestId);

        long count = auditLogRepository.countByRequestId(requestId);

        log.debug("Counted {} audit entries for request {}", count, requestId);

        return count;
    }

    /**
     * Checks if any audit entries exist for a specific request.
     *
     * @param requestId the ID of the approval request
     * @return true if audit entries exist for the request
     * @throws IllegalArgumentException if requestId is null or empty
     */
    public boolean hasAuditEntriesForRequest(String requestId) {
        validateRequestId(requestId);

        boolean hasEntries = auditLogRepository.countByRequestId(requestId) > 0;

        log.debug("Audit entries exist for request {}: {}", requestId, hasEntries);

        return hasEntries;
    }

    /**
     * Calculates the duration between two timestamps.
     *
     * @param start the start timestamp
     * @param end the end timestamp
     * @return a human-readable duration string
     */
    private String calculateDuration(LocalDateTime start, LocalDateTime end) {
        java.time.Duration duration = java.time.Duration.between(start, end);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        StringBuilder durationStr = new StringBuilder();

        if (days > 0) {
            durationStr.append(days).append(" day").append(days > 1 ? "s" : "").append(" ");
        }
        if (hours > 0) {
            durationStr.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(" ");
        }
        if (minutes > 0) {
            durationStr.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(" ");
        }
        if (seconds > 0 || durationStr.length() == 0) {
            durationStr.append(seconds).append(" second").append(seconds != 1 ? "s" : "");
        }

        return durationStr.toString().trim();
    }

    // Validation methods
    private void validateRecordActionParameters(String requestId, String action, String performedById) {
        validateRequestId(requestId);
        validateAction(action);
        validatePerformedById(performedById);
    }

    private void validateRequestId(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
    }

    private void validateAction(String action) {
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Action cannot be null or empty");
        }
    }

    private void validatePerformedById(String performedById) {
        if (performedById == null || performedById.trim().isEmpty()) {
            throw new IllegalArgumentException("Performed by ID cannot be null or empty");
        }
    }

    private void validateTimeRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}
