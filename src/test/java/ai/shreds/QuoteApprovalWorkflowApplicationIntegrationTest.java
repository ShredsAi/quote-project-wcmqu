package ai.shreds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = QuoteApprovalWorkflowApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("test")
@Testcontainers
class QuoteApprovalWorkflowApplicationIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(QuoteApprovalWorkflowApplicationIntegrationTest.class);

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-test-db.sql");

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management");

    @Autowired
    private ApplicationContext applicationContext;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        // RabbitMQ configuration
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
        registry.add("spring.rabbitmq.virtual-host", () -> "/");
        
        // Disable Liquibase for tests
        registry.add("spring.liquibase.enabled", () -> "false");
        
        // JPA configuration for tests
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @Test
    void contextLoads() {
        logger.info("=== Starting Spring Boot Application Context Load Test ===");
        
        // Verify containers are running
        assertThat(postgresContainer.isRunning())
                .as("PostgreSQL container should be running")
                .isTrue();
        
        assertThat(rabbitMQContainer.isRunning())
                .as("RabbitMQ container should be running")
                .isTrue();
        
        logger.info("PostgreSQL container is running at: {}:{}", 
                postgresContainer.getHost(), 
                postgresContainer.getFirstMappedPort());
        
        logger.info("RabbitMQ container is running at: {}:{}", 
                rabbitMQContainer.getHost(), 
                rabbitMQContainer.getAmqpPort());
        
        // Verify application context loads successfully
        assertThat(applicationContext)
                .as("Application context should not be null")
                .isNotNull();
        
        logger.info("Application context loaded successfully with {} beans", 
                applicationContext.getBeanDefinitionCount());
        
        // Verify key beans are present
        assertThat(applicationContext.containsBean("applicationApprovalService"))
                .as("ApplicationApprovalService bean should be present")
                .isTrue();
        
        logger.info("=== Application started successfully ===");
        
        // Log all bean names for debugging
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        logger.info("Total beans loaded: {}", beanNames.length);
        
        // Log container connection details
        logger.info("Database connection: {}", postgresContainer.getJdbcUrl());
        logger.info("RabbitMQ connection: amqp://{}:{}", 
                rabbitMQContainer.getHost(), 
                rabbitMQContainer.getAmqpPort());
    }

    @Test
    void applicationStartsAndHealthCheckPasses() {
        logger.info("=== Starting Application Health Check Test ===");
        
        // Verify the application context is running - the fact that we can access it means it's active
        assertThat(applicationContext)
                .as("Application context should not be null")
                .isNotNull();
        
        // Verify we can get beans from the context, which proves it's active
        assertThat(applicationContext.getBeanDefinitionCount())
                .as("Application context should have beans loaded")
                .isGreaterThan(0);
        
        // Verify containers are running - use isRunning() instead of isHealthy()
        assertThat(postgresContainer.isRunning())
                .as("PostgreSQL container should be running")
                .isTrue();
        
        // Use isRunning() for RabbitMQ as it has no health check
        assertThat(rabbitMQContainer.isRunning())
                .as("RabbitMQ container should be running")
                .isTrue();
        
        logger.info("=== All health checks passed ===");
    }

    @Test
    void verifyKeyBeansAreLoaded() {
        logger.info("=== Verifying Key Application Beans ===");
        
        // Verify adapter beans
        assertThat(applicationContext.containsBean("adapterApprovalController"))
                .as("AdapterApprovalController bean should be present")
                .isTrue();
        
        // Verify application service beans
        assertThat(applicationContext.containsBean("applicationApprovalService"))
                .as("ApplicationApprovalService bean should be present")
                .isTrue();
        
        // Verify domain service beans
        assertThat(applicationContext.containsBean("domainApprovalRequestService"))
                .as("DomainApprovalRequestService bean should be present")
                .isTrue();
        
        // Verify repository beans
        assertThat(applicationContext.containsBean("infrastructureApprovalRequestRepositoryImpl"))
                .as("InfrastructureApprovalRequestRepositoryImpl bean should be present")
                .isTrue();
        
        // Verify configuration beans
        assertThat(applicationContext.containsBean("infrastructureDatabaseConfig"))
                .as("InfrastructureDatabaseConfig bean should be present")
                .isTrue();
        
        assertThat(applicationContext.containsBean("infrastructureRabbitMQConfig"))
                .as("InfrastructureRabbitMQConfig bean should be present")
                .isTrue();
        
        logger.info("=== All key beans are properly loaded ===");
    }
}
