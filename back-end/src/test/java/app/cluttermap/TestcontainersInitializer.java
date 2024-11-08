package app.cluttermap;

import java.time.Duration;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

class TestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.1"))
            .withStartupAttempts(3) // Retry startup if initial attempts fail
            .withStartupTimeout(Duration.ofSeconds(60)); // Allow more time for the container to start

    static {
        try {
            Startables.deepStart(postgres).join();
            System.out.println("PostgreSQL container started at " + postgres.getJdbcUrl());
        } catch (Exception e) {
            System.err.println("Failed to start PostgreSQL container: " + e.getMessage());
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        System.out.println("Initializing Postgres database values for TestContainers...");
        System.out.println("Postgres JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("Postgres Username: " + postgres.getUsername());
        System.out.println("Postgres Password: " + postgres.getPassword());
        TestPropertyValues.of(
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword()).applyTo(ctx.getEnvironment());
        System.out.println("Test properties have been applied to the Spring context.");
    }
}

// https://maciejwalkowiak.com/blog/testcontainers-spring-boot-setup/