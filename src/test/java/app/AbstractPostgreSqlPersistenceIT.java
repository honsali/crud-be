package app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import app.core.exception.ConflictException;
import app.core.exception.StaleVersionException;
import app.core.reference.Reference;
import app.core.security.web.AuthService;
import app.core.security.web.InvalidCredentialsException;
import app.core.security.web.LoginRequest;
import app.domain.admin.account.AccountCreateRequest;
import app.domain.admin.account.AccountResponse;
import app.domain.admin.account.AccountService;
import app.domain.admin.account.AccountUpdateRequest;
import app.domain.admin.account.PasswordResetRequest;
import app.domain.rh.conge.CongeCreateRequest;
import app.domain.rh.conge.CongeResponse;
import app.domain.rh.conge.CongeService;
import app.domain.rh.departement.DepartementCreateRequest;
import app.domain.rh.departement.DepartementResponse;
import app.domain.rh.departement.DepartementService;
import app.domain.rh.employe.EmployeCreateRequest;
import app.domain.rh.employe.EmployeResponse;
import app.domain.rh.employe.EmployeService;
import app.domain.rh.employe.EmployeUpdateRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractPostgreSqlPersistenceIT {

    private static final Pattern SAFE_SCHEMA_NAME = Pattern.compile("rh_it_[0-9a-f]{32}");
    protected static final String ISOLATED_SCHEMA = "rh_it_" + UUID.randomUUID().toString().replace("-", "");

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired DepartementService departementService;
    @Autowired EmployeService employeService;
    @Autowired CongeService congeService;
    @Autowired AccountService accountService;
    @Autowired AuthService authService;
    @Autowired JwtDecoder jwtDecoder;

    protected static void registerIsolatedSchemaProperties(DynamicPropertyRegistry registry) {
        requireSafeSchemaName();
        registry.add("spring.flyway.create-schemas", () -> true);
        registry.add("spring.flyway.default-schema", () -> ISOLATED_SCHEMA);
        registry.add("spring.flyway.schemas", () -> ISOLATED_SCHEMA);
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> ISOLATED_SCHEMA);
    }

    protected static String isolatedJdbcUrl(String jdbcUrl) {
        requireSafeSchemaName();
        if (jdbcUrl == null || jdbcUrl.isBlank() || !jdbcUrl.startsWith("jdbc:postgresql:")) {
            throw new IllegalStateException("TEST_DB_URL doit être une URL JDBC PostgreSQL explicite");
        }
        if (jdbcUrl.toLowerCase(Locale.ROOT).contains("currentschema=")) {
            throw new IllegalStateException("L'URL JDBC de test ne doit pas définir currentSchema");
        }
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "currentSchema=" + ISOLATED_SCHEMA;
    }

    private static void requireSafeSchemaName() {
        if (!SAFE_SCHEMA_NAME.matcher(ISOLATED_SCHEMA).matches()) {
            throw new IllegalStateException("Nom de schéma PostgreSQL de test non sûr: " + ISOLATED_SCHEMA);
        }
    }

    @BeforeEach
    void clearDatabase() {
        assertCurrentSchemaIsIsolated();
        jdbcTemplate.execute("TRUNCATE TABLE " + ISOLATED_SCHEMA + ".account, "
                + ISOLATED_SCHEMA + ".app_role, "
                + ISOLATED_SCHEMA + ".conge, "
                + ISOLATED_SCHEMA + ".type_conge, "
                + ISOLATED_SCHEMA + ".employe, "
                + ISOLATED_SCHEMA + ".situation_familiale, "
                + ISOLATED_SCHEMA + ".sexe, "
                + ISOLATED_SCHEMA + ".departement RESTART IDENTITY CASCADE");
    }

    @AfterAll
    void dropIsolatedSchema() {
        requireSafeSchemaName();
        assertCurrentSchemaIsIsolated();
        jdbcTemplate.execute("DROP SCHEMA " + ISOLATED_SCHEMA + " CASCADE");
    }

    @Test
    void migrationCreatesTheSimplifiedAccountSchema() {
        assertThat(columnExists("account", "password_hash")).isTrue();
        assertThat(columnExists("account", "activated")).isTrue();
        assertThat(columnExists("account", "display_name")).isFalse();
        assertThat(columnExists("account", "email")).isFalse();
        assertThat(tableExists("account_credential")).isFalse();

        jdbcTemplate.update("INSERT INTO departement(nom) VALUES ('Support')");
        assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO departement(nom) VALUES ('Support')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rhCrudPersistsReferencesChildrenAndOptimisticVersions() {
        DepartementResponse departement = departementService.creer(
                new DepartementCreateRequest("Ressources humaines", "Équipe RH"));
        Long sexeId = insertReference("sexe", "Féminin");
        Long situationId = insertReference("situation_familiale", "Célibataire");
        Long typeCongeId = insertReference("type_conge", "Payé");

        EmployeResponse employe = employeService.creer(new EmployeCreateRequest(
                "EMP-001", "Martin", "Alice", LocalDate.of(1990, 5, 12),
                new Reference(sexeId, null), new Reference(situationId, null),
                LocalDate.of(2020, 1, 2), "alice@example.test", "0600000000",
                "Paris", "1 rue du Test", "Gestionnaire", "Profil de démonstration",
                new Reference(departement.id(), null)));
        CongeResponse conge = congeService.creer(employe.id(), new CongeCreateRequest(
                "CONGE-001", new Reference(typeCongeId, null),
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2), "Repos"));

        assertThat(employeService.recupererParId(employe.id()).departement().id()).isEqualTo(departement.id());
        assertThat(congeService.listerParIdEmploye(employe.id())).extracting(CongeResponse::id)
                .containsExactly(conge.id());

        EmployeUpdateRequest stale = new EmployeUpdateRequest(
                employe.matricule(), employe.nom(), employe.prenom(), employe.dateNaissance(),
                employe.sexe(), employe.situationFamiliale(), employe.dateEntree(), employe.email(),
                employe.telephone(), employe.ville(), employe.adresse(), employe.fonction(),
                employe.description(), employe.departement(), employe.version() + 1);
        assertThatThrownBy(() -> employeService.maj(employe.id(), stale))
                .isInstanceOf(StaleVersionException.class);
    }

    @Test
    void accountAdministrationAndLoginUseOneSimpleModel() {
        insertRole("ADMIN", "Administrateur");
        insertRole("GESTIONNAIRE_RH", "Gestionnaire RH");

        AccountResponse account = accountService.creer(new AccountCreateRequest(
                " Alice.Admin ", "password-123", "ROLE_ADMIN"));

        assertThat(account.username()).isEqualTo("alice.admin");
        assertThat(account.role().code()).isEqualTo("ROLE_ADMIN");
        assertThat(account.activated()).isTrue();
        assertThat(accountService.lister()).extracting(AccountResponse::id).containsExactly(account.id());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT password_hash FROM account WHERE id = ?", String.class, account.id()))
                .startsWith("{argon2id}$argon2id$")
                .doesNotContain("password-123");

        String token = authService.login(new LoginRequest("ALICE.ADMIN", "password-123")).accessToken();
        assertThat(jwtDecoder.decode(token).getSubject()).isEqualTo("alice.admin");
        assertThat(jwtDecoder.decode(token).getClaimAsString("role")).isEqualTo("ROLE_ADMIN");

        accountService.reinitialiserMotDePasse(account.id(), new PasswordResetRequest("new-password-123"));
        assertThatThrownBy(() -> authService.login(new LoginRequest("alice.admin", "password-123")))
                .isInstanceOf(InvalidCredentialsException.class);

        AccountResponse updated = accountService.maj(
                account.id(), new AccountUpdateRequest("ROLE_GESTIONNAIRE_RH", false));
        assertThat(updated.role().code()).isEqualTo("ROLE_GESTIONNAIRE_RH");
        assertThat(updated.activated()).isFalse();
        assertThatThrownBy(() -> authService.login(new LoginRequest("alice.admin", "new-password-123")))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThatThrownBy(() -> accountService.creer(
                new AccountCreateRequest("ALICE.ADMIN", "another-password", "ROLE_ADMIN")))
                .isInstanceOf(ConflictException.class);
    }

    private Long insertReference(String table, String libelle) {
        if (!List.of("sexe", "situation_familiale", "type_conge").contains(table)) {
            throw new IllegalArgumentException("Table de référence inconnue: " + table);
        }
        return jdbcTemplate.queryForObject(
                "INSERT INTO " + table + "(libelle) VALUES (?) RETURNING id", Long.class, libelle);
    }

    private void insertRole(String code, String libelle) {
        jdbcTemplate.update("INSERT INTO app_role(code, libelle) VALUES (?, ?)", code, libelle);
    }

    private boolean tableExists(String table) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT to_regclass(?) IS NOT NULL", Boolean.class, ISOLATED_SCHEMA + "." + table));
    }

    private boolean columnExists(String table, String column) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_schema = ? AND table_name = ? AND column_name = ?
                )
                """, Boolean.class, ISOLATED_SCHEMA, table, column));
    }

    private void assertCurrentSchemaIsIsolated() {
        requireSafeSchemaName();
        String currentSchema = jdbcTemplate.queryForObject("SELECT current_schema()", String.class);
        if (!ISOLATED_SCHEMA.equals(currentSchema)) {
            throw new IllegalStateException("Schéma PostgreSQL inattendu: " + currentSchema);
        }
    }
}
