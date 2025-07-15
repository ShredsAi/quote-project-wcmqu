package ai.shreds.quote_approval;

import ai.shreds.QuoteApprovalWorkflowApplication;
import ai.shreds.domain.entities.DomainApprovalQueueEntity;
import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.ports.DomainOutputPortApprovalQueueRepository;
import ai.shreds.domain.ports.DomainOutputPortApprovalRequestRepository;
import ai.shreds.domain.value_objects.DomainApprovalStatus;
import ai.shreds.domain.value_objects.DomainPriority;
import ai.shreds.shared.dtos.SharedQuoteCreatedEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationEventPublisher;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = QuoteApprovalWorkflowApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(OutputCaptureExtension.class)
class QuoteSubmissionIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(QuoteSubmissionIntegrationTest.class);

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
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private DomainOutputPortApprovalRequestRepository approvalRequestRepository;

    @Autowired
    private DomainOutputPortApprovalQueueRepository approvalQueueRepository;

    private DomainApprovalQueueEntity generalQueue;
    private DomainApprovalQueueEntity highPriorityQueue;

    @BeforeEach
    void setUp() {
        logger.info("Setting up test data...");
        
        // Find existing queues from seed data or create new ones
        List<DomainApprovalQueueEntity> existingQueues = approvalQueueRepository.findActiveQueues();
        
        // Find or create general queue
        generalQueue = existingQueues.stream()
                .filter(q -> q.getQueueName().toLowerCase().contains("general"))
                .findFirst()
                .orElseGet(() -> {
                    DomainApprovalQueueEntity queue = new DomainApprovalQueueEntity();
                    queue.setQueueName("General Queue");
                    queue.initializeQueue();
                    queue.setMaxCapacity(1000);
                    return approvalQueueRepository.save(queue);
                });
        
        // Find or create high priority queue
        highPriorityQueue = existingQueues.stream()
                .filter(q -> q.getQueueName().toLowerCase().contains("high") || q.getQueueName().toLowerCase().contains("priority"))
                .findFirst()
                .orElseGet(() -> {
                    DomainApprovalQueueEntity queue = new DomainApprovalQueueEntity();
                    queue.setQueueName("High Priority Queue");
                    queue.initializeQueue();
                    queue.setMaxCapacity(500);
                    return approvalQueueRepository.save(queue);
                });
        
        logger.info("Using general queue with ID: {}", generalQueue.getQueueId());
        logger.info("Using high priority queue with ID: {}", highPriorityQueue.getQueueId());
    }

    @Test
    void whenQuoteCreatedEventReceived_thenApprovalRequestCreatedAndQueued(CapturedOutput output) {
        logger.info("Starting test: whenQuoteCreatedEventReceived_thenApprovalRequestCreatedAndQueued");
        
        UUID quoteId = UUID.randomUUID();
        UUID submittedById = UUID.randomUUID();
        
        // Count existing requests before test
        long initialRequestCount = approvalRequestRepository.count();
        logger.info("Initial request count: {}", initialRequestCount);
        
        SharedQuoteCreatedEventDTO event = new SharedQuoteCreatedEventDTO(
                "QuoteCreatedEvent",
                quoteId.toString(),
                submittedById.toString(),
                DomainPriority.NORMAL.name(),
                Instant.now().toString()
        );
        
        logger.info("Publishing QuoteCreatedEvent for quote ID: {}", quoteId);
        eventPublisher.publishEvent(event);

        // Wait for async processing and check that a new request was created
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            long currentRequestCount = approvalRequestRepository.count();
            logger.info("Current request count: {}", currentRequestCount);
            
            // Verify that a new request was created
            assertThat(currentRequestCount).isGreaterThan(initialRequestCount);
            
            // Try to find the specific request
            DomainApprovalRequestEntity createdRequest = approvalRequestRepository.findByQuoteId(quoteId.toString())
                    .orElseThrow(() -> new AssertionError("Approval request was not created for quote ID: " + quoteId));

            assertThat(createdRequest.getQuoteId()).isEqualTo(quoteId.toString());
            assertThat(createdRequest.getSubmittedById()).isEqualTo(submittedById.toString());
            assertThat(createdRequest.getStatus()).isEqualTo(DomainApprovalStatus.PENDING);
            assertThat(createdRequest.getQueueId()).isEqualTo(generalQueue.getQueueId());
            
            logger.info("Successfully verified approval request creation for quote ID: {}", quoteId);
        });
        
        logger.info("Test completed: whenQuoteCreatedEventReceived_thenApprovalRequestCreatedAndQueued");
    }

    @Test
    void whenHighPriorityQuoteCreated_thenAssignedToPriorityQueueWithAppropriateDeadline(CapturedOutput output) {
        logger.info("Starting test: whenHighPriorityQuoteCreated_thenAssignedToPriorityQueueWithAppropriateDeadline");
        
        UUID quoteId = UUID.randomUUID();
        UUID submittedById = UUID.randomUUID();
        LocalDateTime testStartTime = LocalDateTime.now();
        
        // Count existing requests before test
        long initialRequestCount = approvalRequestRepository.count();
        logger.info("Initial request count: {}", initialRequestCount);
        
        SharedQuoteCreatedEventDTO event = new SharedQuoteCreatedEventDTO(
                "QuoteCreatedEvent",
                quoteId.toString(),
                submittedById.toString(),
                DomainPriority.HIGH.name(),
                Instant.now().toString()
        );
        
        logger.info("Publishing HIGH priority QuoteCreatedEvent for quote ID: {}", quoteId);
        eventPublisher.publishEvent(event);

        // Wait for async processing and verify
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            long currentRequestCount = approvalRequestRepository.count();
            logger.info("Current request count: {}", currentRequestCount);
            
            // Verify that a new request was created
            assertThat(currentRequestCount).isGreaterThan(initialRequestCount);
            
            DomainApprovalRequestEntity createdRequest = approvalRequestRepository.findByQuoteId(quoteId.toString())
                    .orElseThrow(() -> new AssertionError("Approval request was not created for quote ID: " + quoteId));

            // Verify priority is correctly set
            assertThat(createdRequest.getPriority()).isEqualTo(DomainPriority.HIGH);
            logger.info("Verified priority is set to HIGH for quote ID: {}", quoteId);
            
            // Verify assignment to high priority queue
            assertThat(createdRequest.getQueueId()).isEqualTo(highPriorityQueue.getQueueId());
            logger.info("Verified assignment to high priority queue for quote ID: {}", quoteId);
            
            // Verify deadline calculation - HIGH priority should have 24 hours deadline
            assertThat(createdRequest.getDeadline()).isNotNull();
            assertThat(createdRequest.getSubmittedAt()).isNotNull();
            
            // Calculate expected deadline (24 hours from submission time)
            LocalDateTime expectedDeadline = createdRequest.getSubmittedAt().plusHours(24);
            
            // Verify the deadline is within expected range (allowing for small time differences)
            assertThat(createdRequest.getDeadline()).isEqualTo(expectedDeadline);
            
            // Verify that the deadline is indeed 24 hours after submission
            long hoursBetween = ChronoUnit.HOURS.between(createdRequest.getSubmittedAt(), createdRequest.getDeadline());
            assertThat(hoursBetween).isEqualTo(24);
            
            logger.info("Verified deadline calculation: submitted at {}, deadline at {} (24 hours later)", 
                    createdRequest.getSubmittedAt(), createdRequest.getDeadline());
            
            // Verify the deadline is in the future relative to test start time
            assertThat(createdRequest.getDeadline()).isAfter(testStartTime);
            
            logger.info("Successfully verified high priority quote processing with appropriate deadline calculation");
        });
        
        logger.info("Test completed: whenHighPriorityQuoteCreated_thenAssignedToPriorityQueueWithAppropriateDeadline");
    }
}