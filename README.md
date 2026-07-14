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
│   ├── security/          authentication, JWT, CORS, and current-user API
│   ├── referenceData/     shared reference-data query mechanics
│   └── ...                pagination and API exception handling
└── domain/rh/
    ├── employe/
    ├── departement/
    ├── conge/
    ├── sexe/
    ├── situationFamiliale/
    ├── typeConge/
    └── referenceData/     HR-owned reference-data catalog

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

`POST /api/authenticate` is the only public application endpoint. Every other `/api/**` endpoint requires `ROLE_USER` or `ROLE_ADMIN`.

Liquibase imports `liquibase/data/app_user.csv` during the initial migration. The showcase credential is:

```text
admin / admin
```

Before the first migration of a tailored client database, replace it with a unique administrator username and BCrypt password hash. Never deploy the showcase credential.

If the security changeset has already run in a persistent database, do not rewrite it. Add a corrective changeset or provision the account deliberately.

## API overview

All routes use the `/api` prefix.

| Area | Routes |
|---|---|
| Authentication | `POST /authenticate`, `GET /user` |
| Reference data | `GET /reference/{entity}`, with optional ID or allowed-field filtering routes |
| Departments | CRUD under `/departement` |
| Employees | CRUD under `/employe`; filtered pagination through `POST /employe/filtrer` |
| Leave | Create under `/employe/{idEmploye}/conge`; list under `/conge/employe/{idEmploye}`; item CRUD under `/conge/{id}` |

Paginated endpoints return the application-owned `PageResponse<T>` contract rather than exposing Spring's internal page serialization. Request and application failures use Problem Details responses.

JSON request members that are not present in the target DTO are ignored. API date values are accepted and returned as `dd/MM/yyyy`; the shared pattern is configured through `spring.jackson.date-format` in `application.yml` and applied globally to `LocalDate` values by `JsonConfiguration`.

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

There are currently no test sources, so this command validates dependency resolution and compilation. Full-stack E2E orchestration is intended to live in the sibling [`../crud-e2e`](../crud-e2e) project, which will start PostgreSQL, this backend, and the frontend. See [`../WORKSPACE.md`](../WORKSPACE.md#testing-and-verification) for the shared testing strategy and current status.

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
