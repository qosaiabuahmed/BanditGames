package be.kdg.banditgamesbackend.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SyncTaskExecutor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TestcontainersConfiguration.class);
    private final Environment environment;

    public TestcontainersConfiguration(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void logConfiguration() {
        String ciValue = environment.getProperty("CI");
        String rabbitHost = environment.getProperty("spring.rabbitmq.host");
        String rabbitUser = environment.getProperty("spring.rabbitmq.username");

        log.info("=== Test Configuration ===");
        log.info("CI environment variable: {}", ciValue);
        log.info("RabbitMQ Host: {}", rabbitHost);
        log.info("RabbitMQ Username: {}", rabbitUser);
        log.info("Using Testcontainers: {}", ciValue == null || !"true".equals(ciValue));
        log.info("========================");
    }

    @Bean
    @Primary
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster =
                new SimpleApplicationEventMulticaster();

        // Force synchronous execution in tests
        eventMulticaster.setTaskExecutor(new SyncTaskExecutor());
        return eventMulticaster;
    }

    @Bean
    @ServiceConnection
    @ConditionalOnProperty(name = "CI", havingValue = "false", matchIfMissing = true)
    PostgreSQLContainer<?> postgreSQLContainer() {
        // Only create Testcontainers when NOT in CI environment
        // GitLab CI sets CI=true automatically
        return new PostgreSQLContainer<>("postgres:17.6-alpine")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpassword");
    }

    @Bean
    @ServiceConnection
    @ConditionalOnProperty(name = "CI", havingValue = "false", matchIfMissing = true)
    RabbitMQContainer rabbitMQContainer() {
        // Only create Testcontainers when NOT in CI environment
        // GitLab CI sets CI=true automatically
        return new RabbitMQContainer("rabbitmq:3.13.7-management-alpine");
    }

    @org.springframework.test.context.DynamicPropertySource
    static void configurePropertiesForCI(org.springframework.test.context.DynamicPropertyRegistry registry) {
        // When running in CI, use environment variables to configure connections
        // This replaces @ServiceConnection which only works with Testcontainers
        String ciValue = System.getenv("CI");
        if ("true".equals(ciValue)) {
            log.info("CI mode detected - configuring properties from environment variables");

            String rabbitHost = System.getenv("SPRING_RABBITMQ_HOST");
            String rabbitPort = System.getenv("SPRING_RABBITMQ_PORT");
            String rabbitUser = System.getenv("SPRING_RABBITMQ_USERNAME");
            String rabbitPassword = System.getenv("SPRING_RABBITMQ_PASSWORD");
            String datasourceUrl = System.getenv("SPRING_DATASOURCE_URL");
            String datasourceUser = System.getenv("SPRING_DATASOURCE_USERNAME");
            String datasourcePassword = System.getenv("SPRING_DATASOURCE_PASSWORD");

            log.info("RabbitMQ: {}:{} (user: {})", rabbitHost, rabbitPort, rabbitUser);
            log.info("PostgreSQL: {} (user: {})", datasourceUrl, datasourceUser);

            registry.add("spring.rabbitmq.host", () -> rabbitHost);
            registry.add("spring.rabbitmq.port", () -> Integer.parseInt(rabbitPort));
            registry.add("spring.rabbitmq.username", () -> rabbitUser);
            registry.add("spring.rabbitmq.password", () -> rabbitPassword);
            registry.add("spring.datasource.url", () -> datasourceUrl);
            registry.add("spring.datasource.username", () -> datasourceUser);
            registry.add("spring.datasource.password", () -> datasourcePassword);
        }
    }
}
