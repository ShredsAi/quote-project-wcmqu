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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = QuoteApprovalWorkflowApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
        // Clear all existing queues first
        approvalQueueRepository.deleteAll();
        
        // Create test queues with names that match the selection logic
        generalQueue = new DomainApprovalQueueEntity();
        generalQueue.setQueueName("General Queue");
        generalQueue.initializeQueue();
        generalQueue.setMaxCapacity(1000);
        generalQueue = approvalQueueRepository.save(generalQueue);

        highPriorityQueue = new DomainApprovalQueueEntity();
        highPriorityQueue.setQueueName("High Priority Queue");
        highPriorityQueue.initializeQueue();
        highPriorityQueue.setMaxCapacity(500);
        highPriorityQueue = approvalQueueRepository.save(highPriorityQueue);
    }

    @Test
    @Transactional
    void whenQuoteCreatedEventReceived_thenApprovalRequestCreatedAndQueued() {
        UUID quoteId = UUID.randomUUID();
        UUID submittedById = UUID.randomUUID();
        SharedQuoteCreatedEventDTO event = new SharedQuoteCreatedEventDTO(
                "QuoteCreatedEvent",
                quoteId.toString(),
                submittedById.toString(),
                DomainPriority.NORMAL.name(),
                Instant.now().toString()
        );

        eventPublisher.publishEvent(event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            DomainApprovalRequestEntity createdRequest = approvalRequestRepository.findByQuoteId(quoteId.toString())
                    .orElseThrow(() -> new AssertionError("Approval request was not created for quote ID: " + quoteId));

            assertThat(createdRequest.getQuoteId()).isEqualTo(quoteId.toString());
            assertThat(createdRequest.getSubmittedById()).isEqualTo(submittedById.toString());
            assertThat(createdRequest.getStatus()).isEqualTo(DomainApprovalStatus.PENDING);
            assertThat(createdRequest.getQueueId()).isEqualTo(generalQueue.getQueueId());
        });
    }

    @Test
    @Transactional
    void whenHighPriorityQuoteCreated_thenAssignedToPriorityQueue() {
        UUID quoteId = UUID.randomUUID();
        UUID submittedById = UUID.randomUUID();
        SharedQuoteCreatedEventDTO event = new SharedQuoteCreatedEventDTO(
                "QuoteCreatedEvent",
                quoteId.toString(),
                submittedById.toString(),
                DomainPriority.HIGH.name(),
                Instant.now().toString()
        );

        eventPublisher.publishEvent(event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            DomainApprovalRequestEntity createdRequest = approvalRequestRepository.findByQuoteId(quoteId.toString())
                    .orElseThrow(() -> new AssertionError("Approval request was not created for quote ID: " + quoteId));

            assertThat(createdRequest.getPriority()).isEqualTo(DomainPriority.HIGH);
            assertThat(createdRequest.getQueueId()).isEqualTo(highPriorityQueue.getQueueId());
        });
    }
}