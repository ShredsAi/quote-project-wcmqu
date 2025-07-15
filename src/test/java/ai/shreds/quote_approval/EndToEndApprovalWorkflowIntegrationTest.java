package ai.shreds.quote_approval;

import ai.shreds.QuoteApprovalWorkflowApplication;
import ai.shreds.domain.entities.DomainApprovalDecisionEntity;
import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.entities.DomainApprovalQueueEntity;
import ai.shreds.domain.ports.DomainOutputPortApprovalDecisionRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalRequestRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalQueueRepository;
import ai.shreds.domain.ports.DomainOutputPortAuditLogRepository;
import ai.shreds.domain.value_objects.DomainApprovalStatus;
import ai.shreds.domain.value_objects.DomainDecisionType;
import ai.shreds.domain.value_objects.DomainPriority;
import ai.shreds.shared.dtos.SharedApprovalAuditLogDTO;
import ai.shreds.shared.dtos.SharedApprovalDecisionDTO;
import ai.shreds.shared.dtos.SharedApprovalRequestDTO;
import ai.shreds.shared.dtos.SharedQuoteApprovedEventDTO;
import ai.shreds.shared.dtos.SharedQuoteCreatedEventDTO;
import ai.shreds.shared.value_objects.SharedApprovalDecisionRequestParams;
import ai.shreds.shared.value_objects.SharedModeratorAssignmentRequestParams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = QuoteApprovalWorkflowApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(EndToEndApprovalWorkflowIntegrationTest.TestConfig.class)
class EndToEndApprovalWorkflowIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(EndToEndApprovalWorkflowIntegrationTest.class);
    private static final String TEST_QUEUE_PREFIX = "test-e2e-queue-";

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
        
        // Disable Spring Security for tests
        registry.add("spring.security.user.password", () -> "");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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

    private DomainApprovalQueueEntity testQueue;

    private TestRestTemplate authenticatedRestTemplate;

    @BeforeEach
    void setUp() {
        logger.info("Setting up test data...");
        eventListener.reset();
        clearTestDataSafely();

        // Create authenticated rest template
        authenticatedRestTemplate = restTemplate.withBasicAuth("test", "test");

        testQueue = new DomainApprovalQueueEntity();
        testQueue.setQueueId(UUID.randomUUID().toString());
        testQueue.setQueueName(TEST_QUEUE_PREFIX + UUID.randomUUID());
        testQueue.setMaxCapacity(100);
        testQueue.setCurrentSize(0);
        testQueue.setIsActive(true);
        testQueue.setCreatedAt(LocalDateTime.now());
        approvalQueueRepository.save(testQueue);
        logger.info("Test queue created with ID: {}", testQueue.getQueueId());
    }

    @AfterEach
    void tearDown() {
        logger.info("Tearing down test data...");
        clearTestDataSafely();
    }

    void clearTestDataSafely() {
        auditLogRepository.deleteAll();
        approvalDecisionRepository.deleteAll();
        approvalRequestRepository.deleteAll();
        List<DomainApprovalQueueEntity> testQueues = approvalQueueRepository.findAll().stream()
                .filter(q -> q.getQueueName() != null && q.getQueueName().startsWith(TEST_QUEUE_PREFIX))
                .toList();
        if (!testQueues.isEmpty()) {
            approvalQueueRepository.deleteAll(testQueues);
        }
    }

    @Test
    void When_Quote_Submitted_And_Approved_Then_Complete_Workflow_Executed() throws InterruptedException {
        logger.info("=== Starting complete approval workflow test ===");

        // 1. Submit a quote for approval by publishing an event
        String quoteId = UUID.randomUUID().toString();
        String submitterId = UUID.randomUUID().toString();
        SharedQuoteCreatedEventDTO quoteEvent = new SharedQuoteCreatedEventDTO(
                "QuoteCreatedEvent",
                quoteId,
                submitterId,
                DomainPriority.NORMAL.name(),
                Instant.now().toString()
        );
        eventPublisher.publishEvent(quoteEvent);
        logger.info("Published QuoteCreatedEvent for quote ID: {}", quoteId);

        // 2. Verify the approval request is created in the database
        DomainApprovalRequestEntity createdRequest = await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> approvalRequestRepository.findByQuoteId(quoteId).orElse(null), request -> request != null);
        logger.info("Approval request created with ID: {}", createdRequest.getApprovalRequestId());
        assertThat(createdRequest.getStatus()).isEqualTo(DomainApprovalStatus.PENDING);
        assertThat(createdRequest.getQueueId()).isNotNull();

        // 3. Assign a moderator via REST API
        String moderatorId = UUID.randomUUID().toString();
        String requestId = createdRequest.getApprovalRequestId();
        String assignUrl = "/api/approval/" + requestId + "/assign";
        SharedModeratorAssignmentRequestParams assignmentParams = new SharedModeratorAssignmentRequestParams();
        assignmentParams.setModeratorId(moderatorId);
        HttpEntity<SharedModeratorAssignmentRequestParams> assignEntity = new HttpEntity<>(assignmentParams, createJsonHeaders());

        logger.info("Assigning moderator {} to request {}", moderatorId, requestId);
        ResponseEntity<SharedApprovalRequestDTO> assignResponse = authenticatedRestTemplate.exchange(
                assignUrl, HttpMethod.PUT, assignEntity, SharedApprovalRequestDTO.class);

        assertThat(assignResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(assignResponse.getBody()).isNotNull();
        assertThat(assignResponse.getBody().getAssignedModeratorId()).isEqualTo(moderatorId);
        assertThat(assignResponse.getBody().getStatus()).isEqualTo(DomainApprovalStatus.IN_REVIEW.name());

        // 4. Approve the quote via REST API
        String approveUrl = "/api/approval/" + requestId + "/approve";
        SharedApprovalDecisionRequestParams approvalParams = new SharedApprovalDecisionRequestParams();
        approvalParams.setDecision(DomainDecisionType.APPROVED.name());
        approvalParams.setReason("N/A");
        approvalParams.setComments("This quote meets all quality standards.");
        approvalParams.setModeratorId(moderatorId);
        HttpEntity<SharedApprovalDecisionRequestParams> approveEntity = new HttpEntity<>(approvalParams, createJsonHeaders());

        logger.info("Approving request {}", requestId);
        ResponseEntity<SharedApprovalDecisionDTO> approveResponse = authenticatedRestTemplate.exchange(
                approveUrl, HttpMethod.POST, approveEntity, SharedApprovalDecisionDTO.class);

        assertThat(approveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(approveResponse.getBody()).isNotNull();
        assertThat(approveResponse.getBody().getDecision()).isEqualTo(DomainDecisionType.APPROVED.name());

        // 5. Verify the QuoteApprovedEvent was published
        boolean eventReceived = eventListener.await(10, TimeUnit.SECONDS);
        assertThat(eventReceived).isTrue();
        SharedQuoteApprovedEventDTO publishedEvent = eventListener.getLastApprovedEvent();
        assertThat(publishedEvent).isNotNull();
        assertThat(publishedEvent.getQuoteId()).isEqualTo(quoteId);
        assertThat(publishedEvent.getModeratorId()).isEqualTo(moderatorId);
        logger.info("QuoteApprovedEvent received for quote ID: {}", quoteId);

        // 6. Verify final state in the database
        DomainApprovalRequestEntity finalRequest = approvalRequestRepository.findById(requestId).orElseThrow();
        assertThat(finalRequest.getStatus()).isEqualTo(DomainApprovalStatus.APPROVED);

        List<DomainApprovalDecisionEntity> decisions = approvalDecisionRepository.findByRequestId(requestId);
        assertThat(decisions).hasSize(1);
        assertThat(decisions.get(0).getDecision()).isEqualTo(DomainDecisionType.APPROVED);

        // 7. Verify the audit trail
        ParameterizedTypeReference<List<SharedApprovalAuditLogDTO>> auditLogListType = 
                new ParameterizedTypeReference<List<SharedApprovalAuditLogDTO>>() {};
        ResponseEntity<List<SharedApprovalAuditLogDTO>> auditResponse = authenticatedRestTemplate.exchange(
                "/api/approval/audit/" + requestId, HttpMethod.GET, null, auditLogListType);
                
        assertThat(auditResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<SharedApprovalAuditLogDTO> auditLogs = auditResponse.getBody();
        assertThat(auditLogs).isNotNull();
        assertThat(auditLogs).extracting(SharedApprovalAuditLogDTO::getAction).contains("CREATED", "ASSIGNED", "APPROVED");
        logger.info("Audit trail verified with {} entries.", auditLogs.size());
        logger.info("=== Complete approval workflow test finished successfully ===");
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
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
        private volatile SharedQuoteApprovedEventDTO lastApprovedEvent;

        @EventListener
        public void handleQuoteApprovedEvent(SharedQuoteApprovedEventDTO event) {
            logger.info("TestEventListener received QuoteApprovedEvent: {}", event);
            this.lastApprovedEvent = event;
            this.approvedLatch.countDown();
        }

        public SharedQuoteApprovedEventDTO getLastApprovedEvent() {
            return lastApprovedEvent;
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return approvedLatch.await(timeout, unit);
        }

        public void reset() {
            lastApprovedEvent = null;
            approvedLatch = new CountDownLatch(1);
        }
    }
}