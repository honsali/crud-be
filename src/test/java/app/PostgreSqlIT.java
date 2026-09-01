package app;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Pattern;

import app.core.reference.Reference;
import app.core.security.web.AuthService;
import app.core.security.web.LoginRequest;
import app.domain.rh.conge.CongeCreateRequest;
import app.domain.rh.conge.CongeResponse;
import app.domain.rh.conge.CongeService;
import app.domain.rh.departement.DepartementCreateRequest;
import app.domain.rh.departement.DepartementResponse;
import app.domain.rh.departement.DepartementService;
import app.domain.rh.employe.EmployeCreateRequest;
import app.domain.rh.employe.EmployeResponse;
import app.domain.rh.employe.EmployeService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostgreSqlIT {

    private static final Pattern SAFE_SCHEMA_NAME = Pattern.compile("rh_it_[0-9a-f]{32}");
    private static final String SCHEMA = "rh_it_" + UUID.randomUUID().toString().replace("-", "");

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired DepartementService departementService;
    @Autowired EmployeService employeService;
    @Autowired CongeService congeService;
    @Autowired AuthService authService;
    @Autowired JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        requireSafeSchemaName();
        registry.add("spring.datasource.url",
                () -> "jdbc:postgresql://localhost:5432/rh?currentSchema=" + SCHEMA);
        registry.add("spring.datasource.hikari.connection-init-sql",
                () -> "CREATE SCHEMA IF NOT EXISTS " + SCHEMA);
        registry.add("spring.liquibase.default-schema", () -> SCHEMA);
        registry.add("spring.liquibase.liquibase-schema", () -> SCHEMA);
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> SCHEMA);
    }

    @Test
    void startsWithDemoDataAndPersistsTheGeneratedPatterns() {
        assertThat(count("account")).isEqualTo(2);
        assertThat(count("departement")).isEqualTo(10);
        assertThat(count("employe")).isEqualTo(25);

        String token = authService.login(new LoginRequest("admin", "Admin-local-2026!")).accessToken();
        assertThat(jwtDecoder.decode(token).getSubject()).isEqualTo("admin");
        assertThat(jwtDecoder.decode(token).getClaimAsString("role")).isEqualTo("ROLE_ADMIN");

        DepartementResponse departement = departementService.creer(
                new DepartementCreateRequest("Support test", "Département créé par le test"));
        EmployeResponse employe = employeService.creer(new EmployeCreateRequest(
                "TEST-001", "Martin", "Alice", LocalDate.of(1990, 5, 12),
                new Reference(2L, null), new Reference(1L, null),
                LocalDate.of(2020, 1, 2), "alice@example.test", "0600000000",
                "Paris", "1 rue du Test", "Gestionnaire", null,
                new Reference(departement.id(), null)));
        CongeResponse conge = congeService.creer(employe.id(), new CongeCreateRequest(
                "TEST-CONGE-001", new Reference(2L, null),
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2), "Repos"));

        assertThat(departement.id()).isGreaterThanOrEqualTo(100L);
        assertThat(employeService.recupererParId(employe.id()).departement().id())
                .isEqualTo(departement.id());
        assertThat(congeService.listerParIdEmploye(employe.id()))
                .extracting(CongeResponse::id)
                .containsExactly(conge.id());
    }

    @AfterAll
    void dropIsolatedSchema() {
        requireSafeSchemaName();
        assertThat(jdbcTemplate.queryForObject("SELECT current_schema()", String.class)).isEqualTo(SCHEMA);
        jdbcTemplate.execute("DROP SCHEMA " + SCHEMA + " CASCADE");
    }

    private int count(String table) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Integer.class);
    }

    private static void requireSafeSchemaName() {
        if (!SAFE_SCHEMA_NAME.matcher(SCHEMA).matches()) {
            throw new IllegalStateException("Nom de schéma PostgreSQL non sûr: " + SCHEMA);
        }
    }
}
