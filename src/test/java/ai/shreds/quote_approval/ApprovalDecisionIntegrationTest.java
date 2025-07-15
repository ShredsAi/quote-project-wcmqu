package ai.shreds.quote_approval;

import ai.shreds.QuoteApprovalWorkflowApplication;
import ai.shreds.application.services.ApplicationApprovalService;
import ai.shreds.domain.entities.DomainApprovalAuditLogEntity;
import ai.shreds.domain.entities.DomainApprovalDecisionEntity;
import ai.shreds.domain.entities.DomainApprovalQueueEntity;
import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.ports.DomainOutputPortApprovalDecisionRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalQueueRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalRequestRepository;
import ai.shreds.domain.ports.DomainOutputPortAuditLogRepository;
import ai.shreds.domain.value_objects.DomainApprovalStatus;
import ai.shreds.domain.value_objects.DomainDecisionType;
import ai.shreds.domain.value_objects.DomainPriority;
import ai.shreds.shared.dtos.SharedApprovalDecisionDTO;
import ai.shreds.shared.dtos.SharedQuoteApprovedEventDTO;
import ai.shreds.shared.dtos.SharedQuoteRejectedEventDTO;
import ai.shreds.shared.value_objects.SharedApprovalDecisionRequestParams;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
    void When_Moderator_Approves_Quote_Then_Decision_Recorded_And_Event_Published() {
        logger.info("=== Starting approval decision test ===");
        
        String requestId = testApprovalRequest.getApprovalRequestId();
        SharedApprovalDecisionRequestParams approvalParams = new SharedApprovalDecisionRequestParams(
                DomainDecisionType.APPROVED.name(),
                "Good quality quote",
                "Quote meets all standards and guidelines",
                testModeratorId
        );
        
        SharedApprovalDecisionDTO decision = applicationApprovalService.approveQuote(requestId, approvalParams);
        
        assertThat(decision).isNotNull();
        assertThat(decision.getDecisionId()).isNotNull();
        assertThat(decision.getApprovalRequestId()).isEqualTo(requestId);
        assertThat(decision.getModeratorId()).isEqualTo(testModeratorId);
        
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            List<DomainApprovalAuditLogEntity> auditLogs = auditLogRepository.findByRequestId(requestId);
            assertThat(auditLogs).anyMatch(log -> "APPROVED".equals(log.getAction()));
        });
        
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(eventListener.getApprovedEventCount()).isGreaterThan(0);
            SharedQuoteApprovedEventDTO publishedEvent = eventListener.getLastApprovedEvent();
            assertThat(publishedEvent.getQuoteId()).isEqualTo(testQuoteId);
        });

        DomainApprovalRequestEntity updatedRequest = approvalRequestRepository.findById(requestId).get();
        assertThat(updatedRequest.getStatus()).isEqualTo(DomainApprovalStatus.APPROVED);
    }

    @Test
    @Transactional
    void When_Moderator_Rejects_Quote_Then_Decision_Recorded_And_Event_Published() {
        logger.info("=== Starting rejection decision test ===");
        
        String requestId = testApprovalRequest.getApprovalRequestId();
        String rejectionReason = "Inappropriate content";
        SharedApprovalDecisionRequestParams rejectionParams = new SharedApprovalDecisionRequestParams(
                DomainDecisionType.REJECTED.name(),
                rejectionReason,
                "Violates community guidelines",
                testModeratorId
        );
        
        SharedApprovalDecisionDTO decision = applicationApprovalService.rejectQuote(requestId, rejectionParams);
        
        assertThat(decision).isNotNull();
        assertThat(decision.getReason()).isEqualTo(rejectionReason);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            List<DomainApprovalAuditLogEntity> auditLogs = auditLogRepository.findByRequestId(requestId);
            assertThat(auditLogs).anyMatch(log -> "REJECTED".equals(log.getAction()));
        });

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(eventListener.getRejectedEventCount()).isGreaterThan(0);
            SharedQuoteRejectedEventDTO publishedEvent = eventListener.getLastRejectedEvent();
            assertThat(publishedEvent.getQuoteId()).isEqualTo(testQuoteId);
            assertThat(publishedEvent.getReason()).isEqualTo(rejectionReason);
        });

        DomainApprovalRequestEntity updatedRequest = approvalRequestRepository.findById(requestId).get();
        assertThat(updatedRequest.getStatus()).isEqualTo(DomainApprovalStatus.REJECTED);
    }
    
    @Configuration
    static class TestConfig {
        @Bean
        public TestEventListener testEventListener() {
            return new TestEventListener();
        }
    }
    
    static class TestEventListener {
        private CountDownLatch approvedLatch = new CountDownLatch(1);
        private CountDownLatch rejectedLatch = new CountDownLatch(1);
        private SharedQuoteApprovedEventDTO lastApprovedEvent;
        private SharedQuoteRejectedEventDTO lastRejectedEvent;
        private int approvedEventCount = 0;
        private int rejectedEventCount = 0;
        
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