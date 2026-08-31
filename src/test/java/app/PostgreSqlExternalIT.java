package app;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
class PostgreSqlExternalIT extends AbstractPostgreSqlPersistenceIT {

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        requireSchemaManagementConsent();
        registerIsolatedSchemaProperties(registry);
        registry.add("spring.datasource.url", () -> isolatedJdbcUrl(System.getenv("TEST_DB_URL")));
        registry.add("spring.datasource.username", () -> environmentOrDefault("TEST_DB_USERNAME", "postgres"));
        registry.add("spring.datasource.password", () -> environmentOrDefault("TEST_DB_PASSWORD", ""));
    }

    private static void requireSchemaManagementConsent() {
        String consent = System.getenv("TEST_DB_ALLOW_SCHEMA_MANAGEMENT");
        if (consent == null || !"true".equalsIgnoreCase(consent.strip())) {
            throw new IllegalStateException(
                    "TEST_DB_URL est défini, mais TEST_DB_ALLOW_SCHEMA_MANAGEMENT=true est requis "
                            + "avant toute création de schéma");
        }
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null ? defaultValue : value;
    }
}
