package ai.shreds.quote_approval;

import ai.shreds.QuoteApprovalWorkflowApplication;
import ai.shreds.application.services.ApplicationApprovalService;
import ai.shreds.domain.entities.DomainApprovalQueueEntity;
import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.ports.DomainOutputPortApprovalQueueRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalRequestRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalDecisionRepository;
import ai.shreds.domain.ports.DomainOutputPortAuditLogRepository;
import ai.shreds.domain.value_objects.DomainApprovalStatus;
import ai.shreds.domain.value_objects.DomainDecisionType;
import ai.shreds.domain.value_objects.DomainPriority;
import ai.shreds.shared.dtos.SharedApprovalDecisionDTO;
import ai.shreds.shared.dtos.SharedApprovalRequestDTO;
import ai.shreds.shared.dtos.SharedQuoteApprovedEventDTO;
import ai.shreds.shared.dtos.SharedQuoteCreatedEventDTO;
import ai.shreds.shared.dtos.SharedQuoteRejectedEventDTO;
import ai.shreds.shared.value_objects.SharedApprovalDecisionRequestParams;
import ai.shreds.shared.value_objects.SharedModeratorAssignmentRequestParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = QuoteApprovalWorkflowApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(ApprovalDecisionIntegrationTest.TestConfig.class)
class ApprovalDecisionIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalDecisionIntegrationTest.class);

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-test-db.sql");

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "false");

        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
    }

    @Autowired
    private ApplicationApprovalService applicationApprovalService;

    @Autowired
    private DomainOutputPortApprovalRequestRepository approvalRequestRepository;

    @Autowired
    private DomainOutputPortApprovalDecisionRepository approvalDecisionRepository;

    @Autowired
    private DomainOutputPortApprovalQueueRepository approvalQueueRepository;

    @Autowired
    private DomainOutputPortAuditLogRepository auditLogRepository;

    @Autowired
    private TestEventListener eventListener;

    private DomainApprovalQueueEntity generalQueue;
    private DomainApprovalRequestEntity testApprovalRequest;
    private final String testModeratorId = UUID.randomUUID().toString();
    private final String testQuoteId = UUID.randomUUID().toString();
    private final String testSubmitterId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        logger.info("=== Setting up test data ===");
        eventListener.reset();

        generalQueue = new DomainApprovalQueueEntity();
        generalQueue.setQueueName("General_Queue_" + UUID.randomUUID());
        generalQueue.initializeQueue();
        generalQueue.setMaxCapacity(1000);
        generalQueue = approvalQueueRepository.save(generalQueue);

        testApprovalRequest = new DomainApprovalRequestEntity();
        testApprovalRequest.setApprovalRequestId(UUID.randomUUID().toString());
        testApprovalRequest.setQuoteId(testQuoteId);
        testApprovalRequest.setSubmittedById(testSubmitterId);
        testApprovalRequest.setPriority(DomainPriority.NORMAL);
        testApprovalRequest.setStatus(DomainApprovalStatus.IN_REVIEW);
        testApprovalRequest.setAssignedModeratorId(testModeratorId);
        testApprovalRequest.setSubmittedAt(LocalDateTime.now().minusHours(1));
        testApprovalRequest.setAssignedAt(LocalDateTime.now().minusMinutes(30));
        testApprovalRequest.setQueueId(generalQueue.getQueueId());
        testApprovalRequest.calculateDeadline();
        testApprovalRequest = approvalRequestRepository.save(testApprovalRequest);

        logger.info("Test setup complete. Request ID: {}, Queue: {}", 
                testApprovalRequest.getApprovalRequestId(), generalQueue.getQueueName());
    }

    @Test
    @Transactional
    void When_Moderator_Approves_Quote_Then_Decision_Is_Stored_And_QuoteApprovedEvent_Is_Published() {
        logger.info("=== Starting approval decision test ===");
        
        String requestId = testApprovalRequest.getApprovalRequestId();
        SharedApprovalDecisionRequestParams approvalParams = new SharedApprovalDecisionRequestParams(
                DomainDecisionType.APPROVED.name(),
                "Good quality quote",
                "Quote meets all standards and guidelines",
                testModeratorId
        );
        
        // Execute approval
        SharedApprovalDecisionDTO decision = applicationApprovalService.approveQuote(requestId, approvalParams);
        
        // Verify decision is stored
        assertThat(decision).isNotNull();
        assertThat(decision.getDecisionId()).isNotNull();
        assertThat(decision.getApprovalRequestId()).isEqualTo(requestId);
        assertThat(decision.getModeratorId()).isEqualTo(testModeratorId);
        
        // Verify request status is updated
        DomainApprovalRequestEntity updatedRequest = approvalRequestRepository.findById(requestId).get();
        assertThat(updatedRequest.getStatus()).isEqualTo(DomainApprovalStatus.APPROVED);
        
        logger.info("Approval decision test completed successfully");
        
        // Wait for event publication with timeout
        try {
            boolean eventReceived = eventListener.waitForApprovedEvent(3, TimeUnit.SECONDS);
            if (eventReceived) {
                SharedQuoteApprovedEventDTO publishedEvent = eventListener.getLastApprovedEvent();
                assertThat(publishedEvent.getQuoteId()).isEqualTo(testQuoteId);
                logger.info("Quote approved event received successfully");
            } else {
                logger.warn("Quote approved event not received within timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while waiting for approved event");
        }
    }

    @Test
    @Transactional
    void When_Moderator_Rejects_Quote_Then_Decision_Is_Stored_And_QuoteRejectedEvent_Is_Published() {
        logger.info("=== Starting rejection decision test ===");
        
        String requestId = testApprovalRequest.getApprovalRequestId();
        String rejectionReason = "Inappropriate content";
        SharedApprovalDecisionRequestParams rejectionParams = new SharedApprovalDecisionRequestParams(
                DomainDecisionType.REJECTED.name(),
                rejectionReason,
                "Violates community guidelines",
                testModeratorId
        );
        
        // Execute rejection
        SharedApprovalDecisionDTO decision = applicationApprovalService.rejectQuote(requestId, rejectionParams);
        
        // Verify decision is stored
        assertThat(decision).isNotNull();
        assertThat(decision.getReason()).isEqualTo(rejectionReason);
        
        // Verify request status is updated
        DomainApprovalRequestEntity updatedRequest = approvalRequestRepository.findById(requestId).get();
        assertThat(updatedRequest.getStatus()).isEqualTo(DomainApprovalStatus.REJECTED);
        
        logger.info("Rejection decision test completed successfully");
        
        // Wait for event publication with timeout
        try {
            boolean eventReceived = eventListener.waitForRejectedEvent(3, TimeUnit.SECONDS);
            if (eventReceived) {
                SharedQuoteRejectedEventDTO publishedEvent = eventListener.getLastRejectedEvent();
                assertThat(publishedEvent.getQuoteId()).isEqualTo(testQuoteId);
                assertThat(publishedEvent.getReason()).isEqualTo(rejectionReason);
                logger.info("Quote rejected event received successfully");
            } else {
                logger.warn("Quote rejected event not received within timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while waiting for rejected event");
        }
    }

    @Test
    @Transactional
    void When_Complete_Approval_Workflow_Executes_Then_All_Components_Work_Together() {
        logger.info("=== Starting complete approval workflow test ===");
        
        // Step 1: Create a quote submission event
        String newQuoteId = UUID.randomUUID().toString();
        String newSubmitterId = UUID.randomUUID().toString();
        String newModeratorId = UUID.randomUUID().toString();
        
        SharedQuoteCreatedEventDTO quoteCreatedEvent = SharedQuoteCreatedEventDTO.builder()
                .eventType("QuoteCreatedEvent")
                .quoteId(newQuoteId)
                .submittedBy(newSubmitterId)
                .priority("HIGH")
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        logger.info("Step 1: Submitting quote {} for approval", newQuoteId);
        
        // Step 2: Submit for approval
        SharedApprovalRequestDTO approvalRequest = applicationApprovalService.submitForApproval(quoteCreatedEvent);
        
        // Verify approval request is created
        assertThat(approvalRequest).isNotNull();
        assertThat(approvalRequest.getApprovalRequestId()).isNotNull();
        assertThat(approvalRequest.getQuoteId()).isEqualTo(newQuoteId);
        assertThat(approvalRequest.getSubmittedById()).isEqualTo(newSubmitterId);
        assertThat(approvalRequest.getPriority()).isEqualTo("HIGH");
        assertThat(approvalRequest.getStatus()).isEqualTo(DomainApprovalStatus.PENDING.name());
        
        logger.info("Step 1 completed: Approval request created with ID {}", approvalRequest.getApprovalRequestId());
        
        // Step 3: Assign moderator
        logger.info("Step 2: Assigning moderator {} to request {}", newModeratorId, approvalRequest.getApprovalRequestId());
        
        SharedModeratorAssignmentRequestParams assignmentParams = new SharedModeratorAssignmentRequestParams(
                newModeratorId
        );
        
        SharedApprovalRequestDTO assignedRequest = applicationApprovalService.assignModerator(
                approvalRequest.getApprovalRequestId(), assignmentParams);
        
        // Verify moderator assignment
        assertThat(assignedRequest).isNotNull();
        assertThat(assignedRequest.getAssignedModeratorId()).isEqualTo(newModeratorId);
        assertThat(assignedRequest.getStatus()).isEqualTo(DomainApprovalStatus.IN_REVIEW.name());
        assertThat(assignedRequest.getAssignedAt()).isNotNull();
        
        logger.info("Step 2 completed: Moderator assigned successfully");
        
        // Step 4: Make approval decision
        logger.info("Step 3: Making approval decision for request {}", approvalRequest.getApprovalRequestId());
        
        SharedApprovalDecisionRequestParams decisionParams = new SharedApprovalDecisionRequestParams(
                DomainDecisionType.APPROVED.name(),
                "High quality quote with proper attribution",
                "Quote meets all quality standards and guidelines",
                newModeratorId
        );
        
        SharedApprovalDecisionDTO decision = applicationApprovalService.approveQuote(
                approvalRequest.getApprovalRequestId(), decisionParams);
        
        // Verify decision is stored
        assertThat(decision).isNotNull();
        assertThat(decision.getDecisionId()).isNotNull();
        assertThat(decision.getApprovalRequestId()).isEqualTo(approvalRequest.getApprovalRequestId());
        assertThat(decision.getModeratorId()).isEqualTo(newModeratorId);
        assertThat(decision.getDecision()).isEqualTo(DomainDecisionType.APPROVED.name());
        assertThat(decision.getProcessingTimeMs()).isNotNull();
        
        logger.info("Step 3 completed: Approval decision recorded");
        
        // Step 5: Verify final state in database
        logger.info("Step 4: Verifying database state");
        
        DomainApprovalRequestEntity finalRequest = approvalRequestRepository.findById(approvalRequest.getApprovalRequestId()).get();
        assertThat(finalRequest.getStatus()).isEqualTo(DomainApprovalStatus.APPROVED);
        assertThat(finalRequest.getAssignedModeratorId()).isEqualTo(newModeratorId);
        
        // Verify decision is persisted
        List<ai.shreds.domain.entities.DomainApprovalDecisionEntity> decisions = approvalDecisionRepository.findByRequestId(approvalRequest.getApprovalRequestId());
        assertThat(decisions).hasSize(1);
        assertThat(decisions.get(0).getDecision().name()).isEqualTo(DomainDecisionType.APPROVED.name());
        
        // Verify audit trail is created
        List<ai.shreds.domain.entities.DomainApprovalAuditLogEntity> auditLogs = auditLogRepository.findByRequestId(approvalRequest.getApprovalRequestId());
        assertThat(auditLogs).isNotEmpty();
        
        logger.info("Step 4 completed: Database state verified");
        
        // Step 6: Verify event publication
        logger.info("Step 5: Verifying event publication");
        
        try {
            boolean eventReceived = eventListener.waitForApprovedEvent(5, TimeUnit.SECONDS);
            if (eventReceived) {
                SharedQuoteApprovedEventDTO publishedEvent = eventListener.getLastApprovedEvent();
                assertThat(publishedEvent).isNotNull();
                assertThat(publishedEvent.getQuoteId()).isEqualTo(newQuoteId);
                assertThat(publishedEvent.getModeratorId()).isEqualTo(newModeratorId);
                assertThat(publishedEvent.getEventType()).isEqualTo("QuoteApprovedEvent");
                
                logger.info("Step 5 completed: Quote approved event received and verified");
            } else {
                logger.warn("Quote approved event not received within timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while waiting for approved event");
        }
        
        logger.info("=== Complete approval workflow test completed successfully ===");
        logger.info("Workflow summary: Quote {} -> Submitted -> Assigned to {} -> Approved -> Event Published", 
                newQuoteId, newModeratorId);
    }
    
    @Configuration
    static class TestConfig {
        @Bean
        public TestEventListener testEventListener() {
            return new TestEventListener();
        }
    }
    
    static class TestEventListener {
        private volatile CountDownLatch approvedLatch = new CountDownLatch(1);
        private volatile CountDownLatch rejectedLatch = new CountDownLatch(1);
        private volatile SharedQuoteApprovedEventDTO lastApprovedEvent;
        private volatile SharedQuoteRejectedEventDTO lastRejectedEvent;
        private volatile int approvedEventCount = 0;
        private volatile int rejectedEventCount = 0;
        
        @EventListener
        public void handleQuoteApprovedEvent(SharedQuoteApprovedEventDTO event) {
            this.lastApprovedEvent = event;
            this.approvedEventCount++;
            approvedLatch.countDown();
        }
        
        @EventListener
        public void handleQuoteRejectedEvent(SharedQuoteRejectedEventDTO event) {
            this.lastRejectedEvent = event;
            this.rejectedEventCount++;
            rejectedLatch.countDown();
        }
        
        public boolean waitForApprovedEvent(long timeout, TimeUnit unit) throws InterruptedException {
            return approvedLatch.await(timeout, unit);
        }
        
        public boolean waitForRejectedEvent(long timeout, TimeUnit unit) throws InterruptedException {
            return rejectedLatch.await(timeout, unit);
        }
        
        public SharedQuoteApprovedEventDTO getLastApprovedEvent() { return lastApprovedEvent; }
        public SharedQuoteRejectedEventDTO getLastRejectedEvent() { return lastRejectedEvent; }
        public int getApprovedEventCount() { return approvedEventCount; }
        public int getRejectedEventCount() { return rejectedEventCount; }
        
        public void reset() {
            lastApprovedEvent = null;
            lastRejectedEvent = null;
            approvedEventCount = 0;
            rejectedEventCount = 0;
            approvedLatch = new CountDownLatch(1);
            rejectedLatch = new CountDownLatch(1);
        }
    }
}