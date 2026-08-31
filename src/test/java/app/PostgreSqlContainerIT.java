package app;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@Testcontainers
@DisabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
class PostgreSqlContainerIT extends AbstractPostgreSqlPersistenceIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.6-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registerIsolatedSchemaProperties(registry);
        registry.add("spring.datasource.url", () -> isolatedJdbcUrl(POSTGRES.getJdbcUrl()));
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
