# CRUD RH Backend

Minimal Spring Boot REST API used as a complete HR (`ressources humaines`) CRUD showcase and as a reusable foundation for small, tailor-made business applications.

## Project philosophy

This repository is designed for projects developed and maintained by a solo developer for very small businesses.

The main priorities are:

* simplicity;
* explicit and readable code;
* low operational complexity;
* fast customization;
* easy maintenance by both humans and AI agents.

Prefer direct, project-specific implementations over enterprise abstractions, unnecessary genericity, or team-oriented processes.

Do not introduce additional architectural layers, frameworks, profiles, modules, or infrastructure unless they solve a concrete requirement of the current client project.

## Purpose

This repository is an executable project template. It is not intended to be deployed unchanged for a client.

The included HR domain provides a complete working example demonstrating that the following components operate correctly together:

* this Spring Boot backend;
* the associated `../crud-fe` frontend;
* PostgreSQL;
* Liquibase migrations;
* authentication and authorization;
* generated CRUD operations;
* initial CSV data.

The HR application is therefore both a showcase and a verification environment for the complete project stack.

## Client customization workflow

For each new client project:

1. Copy this backend repository and the associated `../crud-fe` frontend.
2. Start the existing HR showcase and verify that the full environment works correctly.
3. Remove all HR-specific backend and frontend artifacts.
4. Remove all HR-specific Liquibase changelogs, database constraints, CSV files, tables, and `master.xml` includes.
5. Generate the actual client domain, migrations, CRUD code, and initial data using `../engine`.
6. Create a fresh database.
7. Start the customized application and verify the complete client workflow.

The disposable HR showcase database must never be reused or promoted into a client environment.

A completed client project must not contain any remaining HR-specific package, class, table, route, migration, CSV file, constraint, or frontend module.

## Generated code

Domain CRUD code is generated from a Java DSL.

Generated code is not treated as an untouchable build artifact. It is intended to be a clean, explicit, and hand-editable foundation that can later be adapted manually or by an AI agent.

Generic behavior belongs in the generator and its templates, not in the generated runtime application.

Generated runtime code should remain:

* explicit;
* easy to understand;
* easy to debug;
* easy to modify;
* specific to the client project.

Avoid replacing straightforward generated code with generic runtime frameworks or abstract reusable layers unless the client project has a concrete need for them.

## Initial data

The RH CSV files in this executable template are intentional showcase fixtures. They make the copied backend and frontend immediately usable, but they are not client production data and must be removed during client customization.

After customization, backend initialization data should contain only:

* reference data required by the application;
* genuine client bootstrap or production data intended to exist in the deployed system.

During development, the database is considered disposable and may be dropped and recreated at any time. Liquibase migrations and CSV imports must therefore be sufficient to rebuild a complete, valid, and usable database from scratch.

E2E tests must start from a normally initialized database and create and remove any temporary records they require. Do not add test or demonstration records to a tailored client's CSV files, Liquibase change sets, or application initialization mechanisms.

## Testing strategy

This project relies exclusively on end-to-end tests.

For this type of application, an E2E test is considered the strongest proof that a feature works correctly because it validates the complete system as it is actually used: frontend, backend, authentication, database, Liquibase migrations, initialization data, and business workflows.

Unit tests are intentionally not added by default. In the context of small applications maintained by a solo developer, they would often duplicate implementation details without providing enough additional confidence to justify their maintenance cost.

Reliability is also built progressively through two complementary mechanisms:

* the same project foundation is reused and improved through successive client projects;
* most CRUD code is produced by a generator whose templates have already been exercised and validated across previous projects.

A defect found in repeated generated code should preferably be corrected in the generator or its templates so that all future generated projects benefit from the fix.

Client-specific behavior and complete user workflows must be validated through E2E tests against a freshly initialized database.

Do not introduce unit tests, mocked tests, or isolated backend test suites automatically. Add another type of test only when it addresses a concrete risk that cannot be adequately covered by the E2E test suite.

### E2E ownership

Full-stack E2E orchestration is intentionally owned by a separate project because it coordinates PostgreSQL, the backend, and the frontend. This repository contains no E2E files. `./mvnw test` currently proves compilation only because there are no test sources. When the separate E2E project is created or available, it should document its exact execution command.

## Development and production environments

This project does not use separate Spring profiles or separate Spring application configurations for development and production.

The environments are defined by where the application runs:

* **development** means the application is running locally on the developer's machine;
* **production** means the same application is running on the deployment server.

Development is intentionally kept as close as possible to production.

Application behavior, Liquibase migrations, database structure, and CSV initialization data are therefore the same in both environments.

Only external runtime values should normally differ, such as:

* database URLs;
* credentials and secrets;
* ports;
* hostnames;
* allowed frontend origins;
* other machine-specific connection settings.

Do not introduce `dev`, `prod`, or similar Spring profiles, profile-specific business behavior, or development-only initialization data unless the project explicitly requires them.

This approach is intentional. The applications are small, maintained by one developer, and deployed for very small businesses. Maintaining multiple application behaviors would add complexity without providing enough value.

## Runtime security

Every launch requires `APP_SECURITY_JWT_BASE64_SECRET`. It must be strict Base64 that decodes to at least 64 bytes; there is no committed fallback or local-only key. `POST /api/authenticate` is the only public application endpoint, and every other `/api/**` endpoint requires `ROLE_USER` or `ROLE_ADMIN`.

Runtime security values are supplied externally:

* `APP_SECURITY_JWT_BASE64_SECRET`;
* `APP_SECURITY_JWT_ISSUER` (default `app_core`);
* `APP_SECURITY_JWT_AUDIENCE` (default `app_core`);
* `APP_SECURITY_TOKEN_VALIDITY_SECONDS` (default `86400`, allowed `1..604800`);
* `APP_CORS_ALLOWED_ORIGINS`;
* `SERVER_ADDRESS` when an explicit bind address is required.

Liquibase loads `liquibase/data/app_user.csv` once during the initial migration. The showcase credential is `admin/admin`. Before the first migration of a tailored client database, replace it with a unique administrator username and BCrypt hash. Never deploy the showcase credential. If the change set has already run, add a corrective change set or provision the account deliberately instead of rewriting the executed migration.

## Database migrations

Liquibase manages the database schema and initial data.

Before delivering a customized client project, it is acceptable to recreate the database and replace the disposable HR migration history with a clean client-specific migration history.

After client migrations have been executed in a persistent environment, preserve the existing Liquibase history.

Do not modify or rewrite already executed change sets. Add new corrective or incremental change sets instead.

For generator testing, `reset-local-db.sql` is the explicit destructive reset. Run it manually only against the disposable local database; normal application startup does not drop the schema.

Current reconstruction safeguards include ordered changelog includes, indexed foreign keys, explicit-ID sequence synchronization, case-insensitive username uniqueness, an RH leave-date ordering check, request-level bounded-text and date-range validation, and Hibernate schema validation after Liquibase. Services own transaction boundaries, JPA relationships are lazy, collection queries use entity graphs where needed, text filters escape SQL LIKE wildcards, and paginated APIs use the stable application-owned `PageResponse<T>` contract.

## Technology stack

* Java 25
* Spring Boot 4.0.6
* Spring Web MVC
* Spring Security
* JWT resource server authentication
* CORS configuration for frontend applications
* Spring Data JPA
* Hibernate 7
* Bean Validation
* Liquibase
* PostgreSQL
* Maven Wrapper

## Intentionally excluded

The following features are intentionally not included:

* auditing;
* caching;
* Spring Boot Actuator;
* OpenAPI or Swagger configuration;
* Spring Boot DevTools;
* notification modules;

Their absence is intentional, not an oversight.

Do not add them automatically. Introduce one of these features only when it is required by a concrete client need.
