package app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import app.core.exception.ConflictException;
import app.core.exception.StaleVersionException;
import app.core.pagination.SortDirection;
import app.core.reference.Reference;
import app.core.security.account.SecurityAccountProvider;
import app.core.security.account.SecurityAccountSnapshot;
import app.core.security.authentication.AuthenticatedAccountPrincipal;
import app.core.security.bootstrap.SecurityBootstrapService;
import app.core.security.config.SecurityProperties;
import app.core.security.credential.AccountCredential;
import app.core.security.credential.CredentialService;
import app.domain.admin.account.AccountCreateRequest;
import app.domain.admin.account.AccountResponse;
import app.domain.admin.account.AccountSearchCriteria;
import app.domain.admin.account.AccountService;
import app.domain.admin.account.AccountSortField;
import app.domain.admin.account.AccountUpdateRequest;
import app.domain.admin.role.RoleCreateRequest;
import app.domain.admin.role.RoleResponse;
import app.domain.admin.role.RoleService;
import app.domain.admin.role.RoleUpdateRequest;
import app.domain.rh.conge.CongeCreateRequest;
import app.domain.rh.conge.CongeResponse;
import app.domain.rh.conge.CongeService;
import app.domain.rh.conge.CongeUpdateRequest;
import app.domain.rh.departement.DepartementCreateRequest;
import app.domain.rh.departement.DepartementResponse;
import app.domain.rh.departement.DepartementService;
import app.domain.rh.employe.EmployeCreateRequest;
import app.domain.rh.employe.EmployeFiltre;
import app.domain.rh.employe.EmployeResponse;
import app.domain.rh.employe.EmployeService;
import app.domain.rh.employe.EmployeUpdateRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractPostgreSqlPersistenceIT {

        private static final Pattern SAFE_SCHEMA_NAME = Pattern.compile("rh_it_[0-9a-f]{32}");
        protected static final String ISOLATED_SCHEMA = "rh_it_" + UUID.randomUUID().toString().replace("-", "");

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
                        throw new IllegalStateException(
                                        "L'URL JDBC de test ne doit pas définir currentSchema; le schéma isolé est géré par la suite");
                }
                return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "currentSchema=" + ISOLATED_SCHEMA;
        }

        private static void requireSafeSchemaName() {
                if (!SAFE_SCHEMA_NAME.matcher(ISOLATED_SCHEMA).matches() || "public".equals(ISOLATED_SCHEMA)) {
                        throw new IllegalStateException("Nom de schéma PostgreSQL de test non sûr: " + ISOLATED_SCHEMA);
                }
        }

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Autowired
        private DepartementService departementService;

        @Autowired
        private EmployeService employeService;

        @Autowired
        private CongeService congeService;

        @Autowired
        private PlatformTransactionManager transactionManager;

        @Autowired
        private RoleService roleService;

        @Autowired
        private AccountService accountService;

        @Autowired
        private SecurityAccountProvider securityAccountProvider;

        @Autowired
        private CredentialService credentialService;

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private SecurityBootstrapService securityBootstrapService;

        @Autowired
        private SecurityProperties securityProperties;

        @Autowired
        private EntityManagerFactory entityManagerFactory;

        @BeforeEach
        void clearDatabase() {
                assertCurrentSchemaIsIsolated();
                securityProperties.getBootstrap().setEnabled(false);
                jdbcTemplate.execute("TRUNCATE TABLE " + ISOLATED_SCHEMA + ".account_credential, "
                                + ISOLATED_SCHEMA + ".account, "
                                + ISOLATED_SCHEMA + ".app_role, "
                                + ISOLATED_SCHEMA + ".conge, "
                                + ISOLATED_SCHEMA + ".type_conge, "
                                + ISOLATED_SCHEMA + ".employe, "
                                + ISOLATED_SCHEMA + ".situation_familiale, "
                                + ISOLATED_SCHEMA + ".sexe, "
                                + ISOLATED_SCHEMA + ".departement RESTART IDENTITY");
        }

        @AfterAll
        void dropIsolatedSchema() {
                requireSafeSchemaName();
                assertCurrentSchemaIsIsolated();
                jdbcTemplate.execute("DROP SCHEMA " + ISOLATED_SCHEMA + " CASCADE");
                Boolean schemaStillExists = jdbcTemplate.queryForObject(
                                "SELECT EXISTS (SELECT 1 FROM pg_namespace WHERE nspname = ?)",
                                Boolean.class,
                                ISOLATED_SCHEMA);
                assertThat(schemaStillExists).isFalse();
        }

        @Test
        void migrationBuildsAnEmptyDatabaseWithTheRequiredConstraints() {
                Long departementId = jdbcTemplate.queryForObject(
                                "INSERT INTO departement(nom) VALUES ('Support') RETURNING id",
                                Long.class);

                assertThatThrownBy(() -> jdbcTemplate.update(
                                "INSERT INTO departement(nom) VALUES ('Support')"))
                                                .isInstanceOf(DataIntegrityViolationException.class)
                                                .rootCause()
                                                .hasMessageContaining("uk_departement_nom");

                Long employeId = jdbcTemplate.queryForObject(
                                """
                                                INSERT INTO employe(matricule, nom, prenom, date_naissance, departement_id)
                                                VALUES ('M-001', 'Martin', 'Alice', '1990-05-12', ?) RETURNING id
                                                """,
                                Long.class,
                                departementId);

                assertThatThrownBy(() -> jdbcTemplate.update(
                                """
                                                INSERT INTO employe(matricule, nom, prenom, date_naissance, departement_id)
                                                VALUES ('M-001', 'Autre', 'Personne', '1991-01-01', ?)
                                                """,
                                departementId))
                                                .isInstanceOf(DataIntegrityViolationException.class);

                assertThatThrownBy(() -> jdbcTemplate.update(
                                """
                                                INSERT INTO employe(matricule, nom, prenom, date_naissance, departement_id)
                                                VALUES ('M-404', 'Sans', 'Département', '1990-01-01', -1)
                                                """))
                                                .isInstanceOf(DataIntegrityViolationException.class);

                assertThatThrownBy(() -> jdbcTemplate.update(
                                "INSERT INTO employe(matricule, nom, prenom) VALUES ('M-NULL', 'Sans', 'Naissance')"))
                                                .isInstanceOf(DataIntegrityViolationException.class);

                Long typeCongeId = insertReference("type_conge", "Congé payé");
                jdbcTemplate.update(
                                """
                                                INSERT INTO conge(
                                                    code, type_conge_id, date_debut_conge,
                                                    date_fin_conge, commentaire, employe_id)
                                                VALUES ('CONGE-001', ?, '2026-08-11', '2026-08-10', 'Repos', ?)
                                                """,
                                typeCongeId,
                                employeId);
                assertThatThrownBy(() -> jdbcTemplate.update(
                                """
                                                INSERT INTO conge(code, type_conge_id, employe_id)
                                                VALUES ('CONGE-001', ?, ?)
                                                """,
                                typeCongeId,
                                employeId))
                                                .isInstanceOf(DataIntegrityViolationException.class)
                                                .rootCause()
                                                .hasMessageContaining("uk_conge_code");
                assertThatThrownBy(() -> jdbcTemplate.update(
                                "INSERT INTO conge(code, type_conge_id, employe_id) VALUES ('CONGE-002', -1, ?)",
                                employeId))
                                                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void departementsAreOrderedAndSearchUsesFiltersPaginationAndAnIdTieBreaker() {
                DepartementResponse support = departementService.creer(new DepartementCreateRequest("Support", null));
                DepartementResponse administration = departementService.creer(
                                new DepartementCreateRequest("Administration", null));
                DepartementResponse ventes = departementService.creer(new DepartementCreateRequest("Ventes", null));

                List<DepartementResponse> departements = departementService.lister();
                assertThat(departements).extracting(DepartementResponse::nom)
                                .containsExactly("Administration", "Support", "Ventes");
                assertThat(departements).extracting(DepartementResponse::id)
                                .containsExactly(administration.id(), support.id(), ventes.id());

                Long sexeId = insertReference("sexe", "Féminin");
                EmployeResponse first = employeService.creer(employeRequest(
                                "M-001", "Martin", "Alice", LocalDate.of(1990, 5, 12), null,
                                new Reference(sexeId, null), new Reference(support.id(), null)));
                EmployeResponse second = employeService.creer(employeRequest(
                                "M-002", "Martin", "Alice", LocalDate.of(1995, 7, 20), "alice.2@example.test",
                                new Reference(sexeId, null), new Reference(support.id(), null)));
                employeService.creer(employeRequest(
                                "M-003", "Martin", "Alice", LocalDate.of(1970, 1, 1), null,
                                new Reference(sexeId, null), new Reference(ventes.id(), null)));

                EmployeFiltre filtre = new EmployeFiltre(
                                "M-0", "mart", "ali",
                                LocalDate.of(1980, 1, 1), LocalDate.of(2000, 12, 31),
                                new Reference(sexeId, null), null,
                                null, null,
                                null, null, null, null, null, null,
                                new Reference(support.id(), null));
                Page<EmployeResponse> firstPage = employeService.filtrer(
                                filtre, PageRequest.of(0, 1, Sort.by("prenom")));
                Page<EmployeResponse> secondPage = employeService.filtrer(
                                filtre, PageRequest.of(1, 1, Sort.by("prenom")));

                assertThat(firstPage.getTotalElements()).isEqualTo(2);
                assertThat(firstPage.getTotalPages()).isEqualTo(2);
                assertThat(firstPage.getContent()).extracting(EmployeResponse::id).containsExactly(first.id());
                assertThat(secondPage.getContent()).extracting(EmployeResponse::id).containsExactly(second.id());
        }

        @Test
        void searchEscapesSqlLikeWildcards() {
                DepartementResponse departement = departementService.creer(new DepartementCreateRequest("Support", null));
                EmployeResponse literal = employeService.creer(employeRequest(
                                "M-001", "A%_\\B", "Alice", LocalDate.of(1990, 1, 1), null,
                                null, new Reference(departement.id(), null)));
                employeService.creer(employeRequest(
                                "M-002", "AxxB", "Bob", LocalDate.of(1990, 1, 1), null,
                                null, new Reference(departement.id(), null)));

                EmployeFiltre filtre = new EmployeFiltre(
                                null, "%_\\", null,
                                null, null, null, null, null, null,
                                null, null, null, null, null, null, null);
                Page<EmployeResponse> result = employeService.filtrer(
                                filtre, PageRequest.of(0, 20, Sort.by("nom")));

                assertThat(result.getContent()).extracting(EmployeResponse::id).containsExactly(literal.id());
        }

        @Test
        void leaveOwnershipOrderingAndOptimisticVersioningAreTransactional() {
                DepartementResponse departement = departementService.creer(new DepartementCreateRequest("Support", null));
                EmployeResponse employe = employeService.creer(employeRequest(
                                "M-001", "Martin", "Alice", LocalDate.of(1990, 1, 1), null,
                                null, new Reference(departement.id(), null)));
                EmployeResponse autreEmploye = employeService.creer(employeRequest(
                                "M-002", "Durand", "Bob", LocalDate.of(1990, 1, 1), null,
                                null, new Reference(departement.id(), null)));
                Long typeCongeId = insertReference("type_conge", "Congé payé");
                Reference typeConge = new Reference(typeCongeId, null);

                CongeResponse later = congeService.creer(employe.id(), new CongeCreateRequest(
                                "CONGE-200", typeConge,
                                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12), "Plus tard"));
                CongeResponse earlier = congeService.creer(employe.id(), new CongeCreateRequest(
                                "CONGE-100", typeConge,
                                LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 10), "Avant"));
                congeService.creer(autreEmploye.id(), new CongeCreateRequest(
                                "CONGE-050", typeConge,
                                LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 6), "Autre employé"));

                assertThat(congeService.listerParIdEmploye(employe.id()))
                                .satisfiesExactly(
                                                conge -> {
                                                        assertThat(conge.id()).isEqualTo(earlier.id());
                                                        assertThat(conge.employe().id()).isEqualTo(employe.id());
                                                },
                                                conge -> {
                                                        assertThat(conge.id()).isEqualTo(later.id());
                                                        assertThat(conge.employe().id()).isEqualTo(employe.id());
                                                });

                CongeResponse updated = congeService.maj(later.id(), new CongeUpdateRequest(
                                later.code(), later.typeConge(), later.dateDebutConge(), later.dateFinConge(),
                                "Commentaire modifié", later.version()));
                assertThat(updated.version()).isEqualTo(later.version() + 1);

                assertThatThrownBy(() -> congeService.maj(later.id(), new CongeUpdateRequest(
                                later.code(), later.typeConge(), later.dateDebutConge(), later.dateFinConge(),
                                "Écrasement", later.version())))
                                                .isInstanceOf(StaleVersionException.class);
                assertThat(congeService.recupererParId(later.id()).commentaire()).isEqualTo("Commentaire modifié");
                assertThat(later.employe().libelle()).isEqualTo("M-001");
        }

        @Test
        void jpaVersionRejectsTwoConcurrentUpdatesThatReadTheSameVersion() throws Exception {
                DepartementResponse departement = departementService.creer(new DepartementCreateRequest("Support", null));
                EmployeResponse employe = employeService.creer(employeRequest(
                                "M-001", "Martin", "Alice", LocalDate.of(1990, 1, 1), null,
                                null, new Reference(departement.id(), null)));

                CountDownLatch bothTransactionsHaveRead = new CountDownLatch(2);
                CountDownLatch startUpdates = new CountDownLatch(1);
                AtomicInteger successes = new AtomicInteger();
                List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
                ExecutorService executor = Executors.newFixedThreadPool(2);
                try {
                        executor.submit(() -> concurrentUpdate(
                                        employe, departement.id(), "Premier", bothTransactionsHaveRead, startUpdates, successes, failures));
                        executor.submit(() -> concurrentUpdate(
                                        employe, departement.id(), "Second", bothTransactionsHaveRead, startUpdates, successes, failures));

                        assertThat(bothTransactionsHaveRead.await(10, TimeUnit.SECONDS)).isTrue();
                        startUpdates.countDown();
                        executor.shutdown();
                        assertThat(executor.awaitTermination(20, TimeUnit.SECONDS)).isTrue();
                } finally {
                        startUpdates.countDown();
                        executor.shutdownNow();
                }

                assertThat(successes).hasValue(1);
                assertThat(failures)
                                .singleElement()
                                .satisfies(failure -> assertThat(hasCause(failure, OptimisticLockingFailureException.class)).isTrue());
                assertThat(employeService.recupererParId(employe.id()).nom()).isIn("Premier", "Second");
        }

        @Test
        void adminMigrationCanonicalizesValuesEnforcesConstraintsAndOrdersRoles() {
                RoleResponse user = roleService.create(new RoleCreateRequest(" user ", " Utilisateur ", null));
                RoleResponse admin = roleService.create(new RoleCreateRequest(" admin ", " Administrateur ", null));

                assertThat(user.code()).isEqualTo("USER");
                assertThat(user.libelle()).isEqualTo("Utilisateur");
                assertThat(roleService.list()).extracting(RoleResponse::code).containsExactly("ADMIN", "USER");
                assertThat(roleService.list()).extracting(RoleResponse::id).containsExactly(admin.id(), user.id());

                AccountResponse alice = accountService.create(new AccountCreateRequest(
                                " Alice.Admin ", " Alice Admin ", " Alice@Example.Test ", true, admin.id()));
                AccountResponse withoutEmailOne = accountService.create(new AccountCreateRequest(
                                "no-email-one", "Sans e-mail 1", "   ", true, user.id()));
                AccountResponse withoutEmailTwo = accountService.create(new AccountCreateRequest(
                                "no-email-two", "Sans e-mail 2", null, false, user.id()));

                assertThat(alice.username()).isEqualTo("alice.admin");
                assertThat(alice.email()).isEqualTo("alice@example.test");
                assertThat(alice.displayName()).isEqualTo("Alice Admin");
                assertThat(alice.role().code()).isEqualTo("ADMIN");
                assertThat(withoutEmailOne.email()).isNull();
                assertThat(withoutEmailTwo.email()).isNull();
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT username FROM account WHERE id = ?", String.class, alice.id()))
                                                .isEqualTo("alice.admin");
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT email FROM account WHERE id = ?", String.class, alice.id()))
                                                .isEqualTo("alice@example.test");

                assertThatThrownBy(() -> roleService.create(new RoleCreateRequest(
                                "AdMiN", "Doublon", null)))
                                                .isInstanceOf(ConflictException.class);
                assertThatThrownBy(() -> accountService.create(new AccountCreateRequest(
                                "ALICE.ADMIN", "Doublon", null, true, user.id())))
                                                .isInstanceOf(ConflictException.class);
                assertThatThrownBy(() -> accountService.create(new AccountCreateRequest(
                                "different", "Doublon e-mail", "ALICE@EXAMPLE.TEST", true, user.id())))
                                                .isInstanceOf(ConflictException.class);

                assertThatThrownBy(() -> jdbcTemplate.update(
                                "INSERT INTO app_role(code, libelle) VALUES (NULL, 'Invalide')"))
                                                .isInstanceOf(DataIntegrityViolationException.class);
                assertThatThrownBy(() -> jdbcTemplate.update(
                                "INSERT INTO app_role(code, libelle) VALUES ('lowercase', 'Invalide')"))
                                                .isInstanceOf(DataIntegrityViolationException.class);
                assertThatThrownBy(() -> jdbcTemplate.update(
                                "INSERT INTO app_role(code, libelle) VALUES ('ADMIN', 'Doublon')"))
                                                .isInstanceOf(DataIntegrityViolationException.class);
                assertThatThrownBy(() -> jdbcTemplate.update(
                                """
                                                INSERT INTO account(username, display_name, active, role_id)
                                                VALUES ('missing-role', 'Sans rôle', true, NULL)
                                                """))
                                                .isInstanceOf(DataIntegrityViolationException.class);
                assertThatThrownBy(() -> jdbcTemplate.update(
                                """
                                                INSERT INTO account(username, display_name, active, role_id)
                                                VALUES ('unknown-role', 'Rôle inconnu', true, -1)
                                                """))
                                                .isInstanceOf(DataIntegrityViolationException.class);
                assertThatThrownBy(() -> jdbcTemplate.update(
                                """
                                                INSERT INTO account(username, display_name, email, active, role_id)
                                                VALUES ('other-user', 'E-mail dupliqué', 'alice@example.test', true, ?)
                                                """,
                                user.id()))
                                                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void accountDeletionIsPhysicalAndAReferencedRoleIsRestricted() {
                RoleResponse role = roleService.create(new RoleCreateRequest("USER", "Utilisateur", null));
                AccountResponse account = accountService.create(new AccountCreateRequest(
                                "physical-delete", "Suppression physique", null, true, role.id()));

                assertThatThrownBy(() -> roleService.delete(role.id()))
                                .isInstanceOf(DataIntegrityViolationException.class);
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT count(*) FROM app_role WHERE id = ?", Long.class, role.id()))
                                                .isOne();

                accountService.delete(account.id(), -1L);
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT count(*) FROM account WHERE id = ?", Long.class, account.id()))
                                                .isZero();

                roleService.delete(role.id());
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT count(*) FROM app_role WHERE id = ?", Long.class, role.id()))
                                                .isZero();
        }

        @Test
        void accountSearchFiltersByRoleAndActiveEscapesWildcardsAndUsesAStableSort() {
                RoleResponse admin = roleService.create(new RoleCreateRequest("ADMIN", "Administrateur", null));
                RoleResponse user = roleService.create(new RoleCreateRequest("USER", "Utilisateur", null));
                AccountResponse first = accountService.create(new AccountCreateRequest(
                                "admin-one", "Même nom", "one@example.test", true, admin.id()));
                AccountResponse second = accountService.create(new AccountCreateRequest(
                                "admin-two", "Même nom", "two@example.test", true, admin.id()));
                AccountResponse inactive = accountService.create(new AccountCreateRequest(
                                "user-inactive", "Inactif", null, false, user.id()));
                AccountResponse literal = accountService.create(new AccountCreateRequest(
                                "literal-name", "A%_\\B", null, true, user.id()));
                accountService.create(new AccountCreateRequest(
                                "ordinary-name", "AxxB", null, true, user.id()));

                Page<AccountResponse> firstPage = accountService.search(new AccountSearchCriteria(
                                null, "même", null, true, admin.id(), 0, 1,
                                AccountSortField.DISPLAY_NAME, SortDirection.ASC));
                Page<AccountResponse> secondPage = accountService.search(new AccountSearchCriteria(
                                null, "même", null, true, admin.id(), 1, 1,
                                AccountSortField.DISPLAY_NAME, SortDirection.ASC));

                assertThat(firstPage.getTotalElements()).isEqualTo(2);
                assertThat(firstPage.getContent()).extracting(AccountResponse::id).containsExactly(first.id());
                assertThat(secondPage.getContent()).extracting(AccountResponse::id).containsExactly(second.id());
                assertThat(firstPage.getContent().getFirst().role().code()).isEqualTo("ADMIN");

                Page<AccountResponse> inactiveUsers = accountService.search(new AccountSearchCriteria(
                                null, null, null, false, user.id(), 0, 20,
                                AccountSortField.USERNAME, SortDirection.ASC));
                assertThat(inactiveUsers.getContent()).extracting(AccountResponse::id).containsExactly(inactive.id());
                assertThat(inactiveUsers.getContent().getFirst().role().id()).isEqualTo(user.id());

                Page<AccountResponse> escaped = accountService.search(new AccountSearchCriteria(
                                null, "%_\\", null, null, null, 0, 20,
                                AccountSortField.DISPLAY_NAME, SortDirection.ASC));
                assertThat(escaped.getContent()).extracting(AccountResponse::id).containsExactly(literal.id());
        }

        @Test
        void accountAndRoleRejectStaleVersionsWithoutCouplingTheirVersions() {
                RoleResponse role = roleService.create(new RoleCreateRequest("USER", "Utilisateur", null));
                AccountResponse account = accountService.create(new AccountCreateRequest(
                                "versioned-user", "Utilisateur versionné", null, true, role.id()));

                RoleResponse updatedRole = roleService.update(role.id(), new RoleUpdateRequest(
                                "Utilisateur renommé", null, role.version()));
                AccountResponse accountAfterRoleUpdate = accountService.get(account.id());
                assertThat(updatedRole.version()).isEqualTo(role.version() + 1);
                assertThat(accountAfterRoleUpdate.version()).isEqualTo(account.version());
                assertThat(accountAfterRoleUpdate.role().libelle()).isEqualTo("Utilisateur renommé");

                assertThatThrownBy(() -> roleService.update(role.id(), new RoleUpdateRequest(
                                "Écrasement rôle", null, role.version())))
                                                .isInstanceOf(StaleVersionException.class);

                AccountResponse updatedAccount = accountService.update(account.id(), new AccountUpdateRequest(
                                account.username(), "Nom modifié", account.email(), false, role.id(), account.version()), -1L);
                assertThat(updatedAccount.version()).isEqualTo(account.version() + 1);
                assertThatThrownBy(() -> accountService.update(account.id(), new AccountUpdateRequest(
                                account.username(), "Écrasement compte", account.email(), true, role.id(), account.version()), -1L))
                                                .isInstanceOf(StaleVersionException.class);
        }

        @Test
        void securityAccountProviderReadsCurrentActiveAndInactiveSnapshotsWithTheirRole() {
                RoleResponse role = roleService.create(new RoleCreateRequest("operator", "Opérateur", null));
                AccountResponse active = accountService.create(new AccountCreateRequest(
                                "active.user", "Compte actif", null, true, role.id()));
                AccountResponse inactive = accountService.create(new AccountCreateRequest(
                                "inactive.user", "Compte inactif", null, false, role.id()));

                SecurityAccountSnapshot activeSnapshot = securityAccountProvider
                                .findByUsername(" Active.User ")
                                .orElseThrow();
                assertThat(activeSnapshot.accountId()).isEqualTo(active.id());
                assertThat(activeSnapshot.username()).isEqualTo("active.user");
                assertThat(activeSnapshot.active()).isTrue();
                assertThat(activeSnapshot.roleCode()).isEqualTo("OPERATOR");

                SecurityAccountSnapshot inactiveSnapshot = securityAccountProvider
                                .findByUsername("INACTIVE.USER")
                                .orElseThrow();
                assertThat(inactiveSnapshot.accountId()).isEqualTo(inactive.id());
                assertThat(inactiveSnapshot.active()).isFalse();
                assertThat(inactiveSnapshot.roleCode()).isEqualTo("OPERATOR");

                assertThat(securityAccountProvider.findById(active.id())).contains(activeSnapshot);
                assertThat(securityAccountProvider.findByUsername("unknown.user")).isEmpty();
                assertThat(securityAccountProvider.findById(Long.MAX_VALUE)).isEmpty();

                roleService.update(role.id(), new RoleUpdateRequest(
                                "Libellé modifié", null, role.version()));
                assertThat(securityAccountProvider.findById(active.id()))
                                .get()
                                .extracting(SecurityAccountSnapshot::roleCode)
                                .isEqualTo("OPERATOR");
        }

        @Test
        void employePersistsEveryDslFieldAndAllowsOptionalValuesToBeCleared() {
                DepartementResponse departement = departementService.creer(
                                new DepartementCreateRequest("Support RH", null));
                Long sexeId = insertReference("sexe", "Féminin");
                Long situationFamilialeId = insertReference("situation_familiale", "Marié(e)");

                EmployeResponse created = employeService.creer(new EmployeCreateRequest(
                                "FULL-001",
                                "Martin",
                                "Alice",
                                LocalDate.of(1990, 5, 12),
                                new Reference(sexeId, null),
                                new Reference(situationFamilialeId, null),
                                LocalDate.of(2020, 9, 1),
                                "Mixed.Case@Example.COM",
                                "0600000000",
                                "Rabat",
                                "10 rue des Fleurs",
                                "Analyste",
                                "Description complète",
                                new Reference(departement.id(), null)));

                assertThat(created.dateNaissance()).isEqualTo(LocalDate.of(1990, 5, 12));
                assertThat(created.sexe()).isEqualTo(new Reference(sexeId, "Féminin"));
                assertThat(created.situationFamiliale())
                                .isEqualTo(new Reference(situationFamilialeId, "Marié(e)"));
                assertThat(created.dateEntree()).isEqualTo(LocalDate.of(2020, 9, 1));
                assertThat(created.email()).isEqualTo("Mixed.Case@Example.COM");
                assertThat(created.telephone()).isEqualTo("0600000000");
                assertThat(created.ville()).isEqualTo("Rabat");
                assertThat(created.adresse()).isEqualTo("10 rue des Fleurs");
                assertThat(created.fonction()).isEqualTo("Analyste");
                assertThat(created.description()).isEqualTo("Description complète");
                assertThat(created.departement()).isEqualTo(new Reference(departement.id(), "Support RH"));

                EmployeFiltre filtreDateEntreeEtSituation = new EmployeFiltre(
                                null, null, null,
                                null, null,
                                null, new Reference(situationFamilialeId, null),
                                LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31),
                                null, null, null, null, null, null, null);
                assertThat(employeService.filtrer(
                                filtreDateEntreeEtSituation, PageRequest.of(0, 20)).getContent())
                                .extracting(EmployeResponse::id)
                                .containsExactly(created.id());

                EmployeResponse cleared = employeService.maj(created.id(), new EmployeUpdateRequest(
                                created.matricule(),
                                created.nom(),
                                created.prenom(),
                                created.dateNaissance(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                created.version()));

                assertThat(cleared.sexe()).isNull();
                assertThat(cleared.situationFamiliale()).isNull();
                assertThat(cleared.dateEntree()).isNull();
                assertThat(cleared.email()).isNull();
                assertThat(cleared.telephone()).isNull();
                assertThat(cleared.ville()).isNull();
                assertThat(cleared.adresse()).isNull();
                assertThat(cleared.fonction()).isNull();
                assertThat(cleared.description()).isNull();
                assertThat(cleared.departement()).isNull();
        }

        @Test
        void referencedDepartementAndEmployeRemainRestrictedNeverCascaded() {
                DepartementResponse departement = departementService.creer(
                                new DepartementCreateRequest("Relations restrictives", null));
                Long sexeId = insertReference("sexe", "Masculin");
                Long situationFamilialeId = insertReference("situation_familiale", "Célibataire");
                EmployeResponse employe = employeService.creer(new EmployeCreateRequest(
                                "FK-001", "Martin", "Alice", LocalDate.of(1990, 1, 1),
                                new Reference(sexeId, null), new Reference(situationFamilialeId, null),
                                null, null, null, null, null, null, null,
                                new Reference(departement.id(), null)));

                assertThatThrownBy(() -> departementService.supprimer(departement.id()))
                                .isInstanceOf(DataIntegrityViolationException.class);
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT count(*) FROM departement WHERE id = ?", Long.class, departement.id()))
                                                .isOne();
                assertThatThrownBy(() -> jdbcTemplate.update("DELETE FROM sexe WHERE id = ?", sexeId))
                                .isInstanceOf(DataIntegrityViolationException.class);
                assertThatThrownBy(() -> jdbcTemplate.update(
                                "DELETE FROM situation_familiale WHERE id = ?", situationFamilialeId))
                                .isInstanceOf(DataIntegrityViolationException.class);

                Long typeCongeId = insertReference("type_conge", "Congé payé");
                congeService.creer(employe.id(), new CongeCreateRequest(
                                "CONGE-PROTEGE", new Reference(typeCongeId, null),
                                LocalDate.of(2026, 8, 11), LocalDate.of(2026, 8, 12), "Congé protégé"));
                assertThatThrownBy(() -> employeService.supprimer(employe.id()))
                                .isInstanceOf(DataIntegrityViolationException.class);
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT count(*) FROM employe WHERE id = ?", Long.class, employe.id()))
                                                .isOne();
                assertThatThrownBy(() -> jdbcTemplate.update("DELETE FROM type_conge WHERE id = ?", typeCongeId))
                                .isInstanceOf(DataIntegrityViolationException.class);

                assertThat(deleteRule("fk_employe_departement")).isEqualTo("RESTRICT");
                assertThat(deleteRule("fk_employe_sexe")).isEqualTo("RESTRICT");
                assertThat(deleteRule("fk_employe_situation_familiale")).isEqualTo("RESTRICT");
                assertThat(deleteRule("fk_conge_employe")).isEqualTo("RESTRICT");
                assertThat(deleteRule("fk_conge_type_conge")).isEqualTo("RESTRICT");
        }

        @Test
        void credentialConstraintsCascadeAndARealStoredHashSupportLogin() {
                RoleResponse admin = roleService.create(new RoleCreateRequest("ADMIN", "Administrateur", null));
                AccountResponse account = accountService.create(new AccountCreateRequest(
                                " Real.Login ", "Connexion réelle", null, true, admin.id()));
                String password = " mot de passe PostgreSQL sûr ";
                credentialService.createInitialCredential(account.id(), password);

                String storedHash = jdbcTemplate.queryForObject(
                                "SELECT password_hash FROM account_credential WHERE account_id = ?", String.class, account.id());
                assertThat(storedHash).startsWith("{argon2id}$argon2id$").doesNotContain(password);
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT token_version FROM account_credential WHERE account_id = ?", Long.class, account.id()))
                                                .isZero();
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT password_changed_at FROM account_credential WHERE account_id = ?", Instant.class, account.id()))
                                                .isNotNull();

                Authentication authentication = authenticationManager.authenticate(
                                UsernamePasswordAuthenticationToken.unauthenticated(" REAL.LOGIN ", password));
                assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedAccountPrincipal.class);
                assertThat(((AuthenticatedAccountPrincipal) authentication.getPrincipal()).accountId()).isEqualTo(account.id());

                assertThatThrownBy(() -> jdbcTemplate.update(
                                """
                                                INSERT INTO account_credential(account_id, password_hash, password_changed_at)
                                                VALUES (?, '{argon2id}duplicate', now())
                                                """, account.id()))
                                                .isInstanceOf(DataIntegrityViolationException.class);
                assertThatThrownBy(() -> jdbcTemplate.update(
                                """
                                                INSERT INTO account_credential(account_id, password_hash, password_changed_at)
                                                VALUES (-1, '{argon2id}unknown', now())
                                                """))
                                                .isInstanceOf(DataIntegrityViolationException.class);

                accountService.delete(account.id(), -1L);
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT count(*) FROM account_credential WHERE account_id = ?", Long.class, account.id()))
                                                .isZero();
        }

        @Test
        void credentialJpaVersionRejectsConcurrentRevocations() throws Exception {
                RoleResponse user = roleService.create(new RoleCreateRequest("USER", "Utilisateur", null));
                AccountResponse account = accountService.create(new AccountCreateRequest(
                                "concurrent.user", "Concurrent", null, true, user.id()));
                credentialService.createInitialCredential(account.id(), "mot de passe concurrent sûr");
                Long credentialId = jdbcTemplate.queryForObject(
                                "SELECT id FROM account_credential WHERE account_id = ?", Long.class, account.id());

                CountDownLatch bothRead = new CountDownLatch(2);
                CountDownLatch update = new CountDownLatch(1);
                AtomicInteger successes = new AtomicInteger();
                List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
                ExecutorService executor = Executors.newFixedThreadPool(2);
                try {
                        executor.submit(() -> concurrentCredentialRevocation(credentialId, bothRead, update, successes, failures));
                        executor.submit(() -> concurrentCredentialRevocation(credentialId, bothRead, update, successes, failures));
                        assertThat(bothRead.await(10, TimeUnit.SECONDS)).isTrue();
                        update.countDown();
                        executor.shutdown();
                        assertThat(executor.awaitTermination(20, TimeUnit.SECONDS)).isTrue();
                } finally {
                        update.countDown();
                        executor.shutdownNow();
                }

                assertThat(successes).hasValue(1);
                assertThat(failures).singleElement().satisfies(failure -> assertThat(hasCause(failure, jakarta.persistence.OptimisticLockException.class)).isTrue());
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT version FROM account_credential WHERE id = ?", Long.class, credentialId)).isEqualTo(1L);
                assertThat(jdbcTemplate.queryForObject(
                                "SELECT token_version FROM account_credential WHERE id = ?", Long.class, credentialId)).isEqualTo(1L);
        }

        @Test
        void bootstrapReusesAdminCreatesEverythingOnceAndThenFailsClosed() {
                roleService.create(new RoleCreateRequest("ADMIN", "Administrateur existant", null));
                SecurityProperties.Bootstrap bootstrap = securityProperties.getBootstrap();
                bootstrap.setEnabled(true);
                bootstrap.setUsername(" First.Admin ");
                bootstrap.setDisplayName("Premier administrateur");
                bootstrap.setPassword(" mot de passe bootstrap sûr ");
                try {
                        securityBootstrapService.bootstrapIfEnabled();
                        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM app_role", Long.class)).isOne();
                        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM account", Long.class)).isOne();
                        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM account_credential", Long.class)).isOne();
                        assertThat(securityAccountProvider.findByUsername("first.admin"))
                                        .get().extracting(SecurityAccountSnapshot::roleCode).isEqualTo("ADMIN");

                        assertThatThrownBy(securityBootstrapService::bootstrapIfEnabled)
                                        .isInstanceOf(IllegalStateException.class)
                                        .hasMessageContaining("retirez les variables APP_SECURITY_BOOTSTRAP_*");
                        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM account", Long.class)).isOne();
                        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM account_credential", Long.class)).isOne();
                } finally {
                        bootstrap.setEnabled(false);
                        bootstrap.setPassword("");
                }
        }

        private void concurrentUpdate(
                        EmployeResponse employe,
                        Long departementId,
                        String nom,
                        CountDownLatch bothTransactionsHaveRead,
                        CountDownLatch startUpdates,
                        AtomicInteger successes,
                        List<Throwable> failures) {
                try {
                        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                                EmployeResponse snapshot = employeService.recupererParId(employe.id());
                                bothTransactionsHaveRead.countDown();
                                try {
                                        if (!startUpdates.await(10, TimeUnit.SECONDS)) {
                                                throw new IllegalStateException("Les deux transactions n'ont pas été synchronisées");
                                        }
                                } catch (InterruptedException exception) {
                                        Thread.currentThread().interrupt();
                                        throw new IllegalStateException("Test concurrent interrompu", exception);
                                }
                                employeService.maj(employe.id(), new EmployeUpdateRequest(
                                                employe.matricule(),
                                                nom,
                                                employe.prenom(),
                                                employe.dateNaissance(),
                                                employe.sexe(),
                                                employe.situationFamiliale(),
                                                employe.dateEntree(),
                                                employe.email(),
                                                employe.telephone(),
                                                employe.ville(),
                                                employe.adresse(),
                                                employe.fonction(),
                                                employe.description(),
                                                new Reference(departementId, null),
                                                snapshot.version()));
                        });
                        successes.incrementAndGet();
                } catch (Throwable failure) {
                        failures.add(failure);
                }
        }

        private EmployeCreateRequest employeRequest(
                        String matricule,
                        String nom,
                        String prenom,
                        LocalDate dateNaissance,
                        String email,
                        Reference sexe,
                        Reference departement) {
                return new EmployeCreateRequest(
                                matricule,
                                nom,
                                prenom,
                                dateNaissance,
                                sexe,
                                null,
                                null,
                                email,
                                null,
                                null,
                                null,
                                null,
                                null,
                                departement);
        }

        private Long insertReference(String referenceTable, String libelle) {
                String table = switch (referenceTable) {
                        case "sexe" -> "sexe";
                        case "situation_familiale" -> "situation_familiale";
                        case "type_conge" -> "type_conge";
                        default -> throw new IllegalArgumentException("Table de référence inconnue: " + referenceTable);
                };
                return jdbcTemplate.queryForObject(
                                "INSERT INTO " + table + "(libelle) VALUES (?) RETURNING id",
                                Long.class,
                                libelle);
        }

        private String deleteRule(String constraintName) {
                return jdbcTemplate.queryForObject(
                                """
                                                SELECT delete_rule
                                                FROM information_schema.referential_constraints
                                                WHERE constraint_schema = ? AND constraint_name = ?
                                                """,
                                String.class,
                                ISOLATED_SCHEMA,
                                constraintName);
        }

        private void concurrentCredentialRevocation(
                        Long credentialId,
                        CountDownLatch bothRead,
                        CountDownLatch update,
                        AtomicInteger successes,
                        List<Throwable> failures) {
                EntityManager entityManager = entityManagerFactory.createEntityManager();
                try {
                        entityManager.getTransaction().begin();
                        AccountCredential credential = entityManager.find(AccountCredential.class, credentialId);
                        bothRead.countDown();
                        if (!update.await(10, TimeUnit.SECONDS)) {
                                throw new IllegalStateException("Les lectures concurrentes de credential n'ont pas été synchronisées");
                        }
                        credential.revokeAllTokens();
                        entityManager.getTransaction().commit();
                        successes.incrementAndGet();
                } catch (Throwable failure) {
                        if (entityManager.getTransaction().isActive()) {
                                entityManager.getTransaction().rollback();
                        }
                        failures.add(failure);
                } finally {
                        entityManager.close();
                }
        }

        private boolean hasCause(Throwable failure, Class<? extends Throwable> expectedType) {
                Throwable current = failure;
                while (current != null) {
                        if (expectedType.isInstance(current)) {
                                return true;
                        }
                        current = current.getCause();
                }
                return false;
        }

        private void assertCurrentSchemaIsIsolated() {
                requireSafeSchemaName();
                String currentSchema = jdbcTemplate.queryForObject("SELECT current_schema()", String.class);
                if (!ISOLATED_SCHEMA.equals(currentSchema)) {
                        throw new IllegalStateException(
                                        "Nettoyage PostgreSQL refusé: current_schema() vaut '"
                                                        + currentSchema + "' au lieu de '" + ISOLATED_SCHEMA + "'");
                }
        }
}
