# CRUD RH Backend

Runnable Spring Boot REST API for the HR (`ressources humaines`) showcase. It provides the backend foundation used with the sibling `../crud-fe` frontend and generated with support from `../engine`.

The shared premise and decision principles are documented in [`../Context.md`](../Context.md). Cross-project ownership, generation workflow, and client-customization rules are documented in [`../WORKSPACE.md`](../WORKSPACE.md).

## Responsibilities

This project owns:

- the executable REST API;
- authentication and authorization;
- request and business validation;
- transaction boundaries;
- PostgreSQL persistence;
- Liquibase schema migrations;
- backend initialization data;
- the runtime implementation of generated and hand-adapted backend code.

Liquibase is authoritative for the physical database schema. Hibernate validates that the mapped entities agree with that schema at startup.

## Showcase domain

The included HR domain contains:

- `Employe`;
- `Departement`;
- `Conge`;
- reference entities `Sexe`, `SituationFamiliale`, and `TypeConge`.

It demonstrates generated CRUD operations, filtered and paginated employee search, parent/child leave operations, generic reference-data lookup, validation, security, and database reconstruction from migrations and CSV data.

The showcase is a reusable example and verification environment. It is not intended to be deployed unchanged for a client.

## Technology stack

- Java 25
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Security
- JWT resource server authentication
- Spring Data JPA
- Hibernate 7
- Bean Validation
- Liquibase
- PostgreSQL
- Maven Wrapper

## Project structure

```text
src/main/java/app/
├── CoreApplication.java
├── core/
│   ├── configuration/     shared runtime and HTTP security configuration
│   ├── exception/         API, validation, and security exception handling
│   ├── pagination/        stable pageable handling and page responses
│   ├── persistence/       shared persistence query helpers
│   ├── referenceData/     shared reference-data query mechanics
│   └── security/
│       ├── account/       account persistence and administration
│       └── login/         password login and JWT authentication
└── domain/rh/
    ├── employe/
    ├── departement/
    ├── conge/
    ├── sexe/
    ├── situationFamiliale/
    ├── typeConge/
    └── referenceData/     HR-owned reference-data route and catalog

src/main/resources/
├── application.yml
└── liquibase/
    ├── master.xml
    ├── changelog/
    └── data/
```

Each writable HR feature keeps its entity, DTO, mapper, repository, service, and REST resource together. Shared runtime mechanics belong under `app.core`; client-specific names, labels, filters, and business behavior remain under `app.domain`.

## Requirements

- JDK 25
- PostgreSQL
- a strict Base64 JWT secret that decodes to at least 64 bytes

The default local database connection is:

```text
URL:      jdbc:postgresql://localhost:5432/crud_db
Username: crud
Password: crud
```

Override those values with environment variables when the local PostgreSQL installation differs.

## Running locally

Set the required JWT secret, then start Spring Boot.

### PowerShell

```powershell
$env:APP_SECURITY_JWT_BASE64_SECRET = "<base64-secret>"
.\mvnw.cmd spring-boot:run
```

### Bash

```bash
export APP_SECURITY_JWT_BASE64_SECRET='<base64-secret>'
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080` by default. Liquibase applies the schema and showcase data automatically before Hibernate validates the mappings.

`run.bat` performs the same startup for the original local Windows installation. It contains a machine-specific `JAVA_HOME` and should be adjusted before being used elsewhere.

## Runtime configuration

Configuration is externalized through standard Spring Boot environment variables.

| Variable | Default | Purpose |
|---|---|---|
| `APP_SECURITY_JWT_BASE64_SECRET` | none | Required HS512 secret; strict Base64 decoding to at least 64 bytes. |
| `APP_SECURITY_JWT_ISSUER` | `app_core` | Required JWT issuer. |
| `APP_SECURITY_JWT_AUDIENCE` | `app_core` | Required JWT audience. |
| `APP_SECURITY_TOKEN_VALIDITY_SECONDS` | `86400` | Token lifetime; allowed range `1..604800`. |
| `APP_CORS_ALLOWED_ORIGINS` | local ports `3000,4200,5173,9000` | Comma-separated frontend origins. |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/crud_db` | PostgreSQL JDBC URL. |
| `SPRING_DATASOURCE_USERNAME` | `crud` | Database username. |
| `SPRING_DATASOURCE_PASSWORD` | `crud` | Database password. |
| `SERVER_PORT` | `8080` | HTTP port. |
| `SERVER_ADDRESS` | Spring Boot default | Optional explicit bind address. |
| `SPRING_DATA_WEB_PAGEABLE_DEFAULT_PAGE_SIZE` | `20` | Default employee page size. |
| `SPRING_DATA_WEB_PAGEABLE_MAX_PAGE_SIZE` | `100` | Maximum accepted page size. |
| `SPRING_JPA_SHOW_SQL` | `false` | SQL logging. |
| `SPRING_LIQUIBASE_ENABLED` | `true` | Liquibase execution. |

This project deliberately has no `dev` or `prod` Spring profile. The same application runs in both environments; only external runtime values differ.

## Security

Every launch requires `APP_SECURITY_JWT_BASE64_SECRET`; there is no committed fallback key.

Each account has exactly one role:

| Role | Access |
|---|---|
| `ROLE_GESTIONNAIRE_RH` | Current reference-data, department, employee, and leave APIs. |
| `ROLE_ADMIN` | Account administration only. It cannot access HR business APIs. |

Roles are mutually exclusive. A person who performs both business and account-administration work uses two separate accounts. The backend enforces this in the database, JWT validation, route authorization, and account-management service; frontend role display is not a security boundary.

`POST /api/login` is public and returns the signed bearer token as `accessToken`. `/api/admin/accounts/**` requires `ROLE_ADMIN`, while the complete `/api/rh/**` namespace requires `ROLE_GESTIONNAIRE_RH`. Other `/api/**` routes are denied until assigned explicitly.

JWT `sub` and scalar `role` claims identify the authenticated account; `aid` and `ver` support account validation and token invalidation. Every authenticated request verifies the current database account, activation state, role, and token version. Changing a role, changing activation, or resetting a password therefore invalidates previously issued tokens immediately.

The consolidated `security_table.xml` baseline creates the singular constrained `role` column and token version directly. It seeds two separate showcase accounts:

```text
admin / admin
gestionnaire-rh / gestionnaire-rh
```

This security baseline requires a fresh disposable database. Do not reuse a database created from a different baseline.

Before the first migration of a tailored client database, replace both showcase accounts with deliberate client accounts and unique BCrypt password hashes. Never deploy showcase credentials.

## API overview

All routes use the `/api` prefix. HR routes share the `/api/rh` namespace so authorization does not depend on enumerating entities.

| Area | Routes |
|---|---|
| Authentication | `POST /login` |
| Account administration | `GET` and `POST /admin/accounts`; `PUT /admin/accounts/{id}`; `PUT /admin/accounts/{id}/password` |
| Reference data | `GET /rh/reference/{entity}`, with optional ID or allowed-field filtering routes |
| Departments | CRUD under `/rh/departement` |
| Employees | CRUD under `/rh/employe`; filtered pagination through `POST /rh/employe/filtrer` |
| Leave | Create under `/rh/employe/{idEmploye}/conge`; list under `/rh/conge/employe/{idEmploye}`; item CRUD under `/rh/conge/{id}` |

Account creation accepts `username`, `password`, and one canonical string `role`; numeric enum ordinals are rejected. New accounts are activated initially. Account update accepts `role` and `activated`. Passwords supplied to create/reset operations must contain 8 to 256 characters and fit BCrypt's 72-byte UTF-8 limit. Accounts are deactivated rather than deleted. The service prevents self-demotion/self-deactivation and preserves at least one active administrator.

Paginated endpoints return the application-owned `PageResponse<T>` contract rather than exposing Spring's internal page serialization. Request and application failures use Problem Details responses.

JSON request members that are not present in the target DTO are ignored. API-facing identifiers remain `Long` in Java but are marked with the host-owned `@JsonId` annotation and serialized as JSON strings. This keeps JPA and repository types numeric while giving browser clients stable string IDs; unannotated `Long` values remain JSON numbers, and null IDs remain null. Jackson accepts the corresponding string IDs when DTOs are sent back.

API date values are accepted and returned as `dd/MM/yyyy`; the shared pattern is configured through `spring.jackson.date-format` in `application.yml` and applied globally to `LocalDate` values by `JsonConfiguration`.

## Persistence and implementation conventions

Current backend safeguards include:

- ordered Liquibase changelog includes;
- indexed foreign keys;
- explicit-ID sequence synchronization after CSV imports;
- case-insensitive username uniqueness;
- database constraints for uniqueness, relationships, and leave-date ordering;
- bounded request text and date-range validation;
- Hibernate schema validation after Liquibase;
- service-owned transaction boundaries;
- disabled Open Session in View;
- lazy JPA relationships;
- entity graphs for collection queries that require related display data;
- escaped SQL `LIKE` wildcard input;
- stable pagination sorting.

Database uniqueness constraints remain the final protection against concurrent duplicate creation. Service-level prechecks provide clearer normal-case conflict responses, while database integrity failures are also mapped to HTTP `409`.

Do not duplicate physical schema limits indiscriminately in generated entity metadata. Writable request DTOs own request-shape validation; Liquibase owns the database definition.

## Database reconstruction and migration rules

A disposable local database can be rebuilt from `liquibase/master.xml` and the CSV files under `liquibase/data`.

`reset-local-db.sql` drops and recreates the PostgreSQL `public` schema. It is intentionally destructive and must be run manually only against the disposable local database. Normal application startup never drops the schema.

Recreate the disposable local database before running the current security baseline.

Before delivering a new client project, it is acceptable to recreate the database and replace the disposable HR migration history with a clean client baseline. Once changesets have executed in a persistent environment:

- preserve their IDs and contents;
- do not rewrite them;
- add corrective or incremental changesets instead.

## Validation

Run the backend validation command with:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

This command compiles the backend and runs focused account-policy, JWT-invalidation, role-JSON, JSON-ID, signed-token route-matrix, and changelog-validation tests. Full-stack E2E orchestration is intended to live in the sibling [`../crud-e2e`](../crud-e2e) project, which will start PostgreSQL, this backend, and the frontend. See [`../WORKSPACE.md`](../WORKSPACE.md#testing-and-verification) for the shared testing strategy and current status.

## Backend customization

When adapting this repository for a client:

1. verify the complete HR showcase first;
2. remove `app.domain.rh` and replace it with the client domain;
3. remove HR-specific changelogs, constraints, CSV files, tables, and `master.xml` includes;
4. adapt the domain-owned `ReferenceDataCatalog` implementation;
5. review and transfer the desired backend overlay generated by `../engine`;
6. create a fresh database;
7. start the customized backend and verify it with the corresponding frontend.

A completed client backend must not retain HR-specific packages, classes, tables, routes, migrations, constraints, or initialization data.

## Intentionally excluded

The backend intentionally does not include:

- auditing;
- caching;
- Spring Boot Actuator;
- OpenAPI or Swagger configuration;
- Spring Boot DevTools;
- notification modules.

Their absence is deliberate. Add one only when a concrete client requirement justifies its implementation and maintenance cost.
