# CRUD RH Backend

Minimal Spring Boot REST API for a small HR (`ressources humaines`) CRUD showcase.

Personal/solo-developer project: favor simple, direct, maintainable choices over enterprise or team-oriented process.

## Purpose and tailoring workflow

This repository is a tailor-style executable template, not an application intended to be deployed unchanged. The RH domain and data provide a complete example that proves the generated backend, the tied `../crud-fe` frontend, PostgreSQL, Liquibase, authentication, and CRUD flows work together.

For a new client:

1. copy this backend and the tied frontend;
2. launch the RH showcase to verify the complete environment;
3. remove the RH backend and frontend domain artifacts;
4. remove the RH Liquibase changelogs, constraints, CSV files, and `master.xml` includes;
5. generate the real client domain and client data with `../engine`;
6. recreate a fresh database and verify the tailored client application.

The disposable RH database must not be promoted into a client environment. A tailored output should contain no residual RH package, table, changelog, CSV, route, or frontend module.

## Generated code and data

The domain CRUD code is generated from a Java DSL, but the generated output is intended to be a clean, explicit, hand-editable foundation for future AI/manual tweaks. Genericity belongs in the generator/templates, not in the generated runtime code.

The unconditional RH CSV loads are intentional showcase fixtures so this template starts in a functional state. Generated client data may also load unconditionally when it is genuine client bootstrap/reference data intended for deployment. A baseline/demo distinction is needed only when a client project contains both production seed data and temporary fake verification records.

Once client migrations have run in a persistent environment, preserve Liquibase history and add new corrective change sets rather than rewriting executed migrations.

## JWT secret

Every launch, on the laptop or server, requires `APP_SECURITY_JWT_BASE64_SECRET`. It must be strict Base64 decoding to at least 64 bytes. There is no committed fallback, temporary local JWT key, or laptop-specific Spring profile; `run.bat` stops with an explanation when the variable is missing.

## Initial administrator

Liquibase loads `liquibase/data/app_user.csv` automatically during the initial database migration. The showcase credential is `admin/admin`.

Before the first migration of the tailored client/server database:

1. choose a unique administrator username and strong password;
2. generate a BCrypt cost-12 hash, for example with `docker run --rm -it httpd:2.4-alpine htpasswd -nBC 12 <username>`;
3. put the username and hash—not the clear password—in `app_user.csv`;
4. start the server normally and let Liquibase create the account.

Liquibase records the change set, so later startups do not insert the account again. Never deploy the showcase `admin/admin` credential. If the database was already migrated, editing the CSV is too late; add a new migration or insert the account deliberately.

## Resetting the local database

For generator testing, run `reset-local-db.sql` manually against the disposable local PostgreSQL database. It drops and recreates the complete `public` schema. Normal application startup does not reset the database, so always verify the target before running the script.

## Database integrity

Generated foreign keys are indexed, explicit-ID CSV loads synchronize their sequences, usernames are unique ignoring case, and the RH leave dates have an explicit ordering check. Hibernate validates the generated schema after Liquibase at startup. Optimistic locking is intentionally omitted for the current small-client, low-concurrency scope.

## Pagination contract

Paginated API endpoints return the application-owned flat `PageResponse<T>` contract expected by the frontend. Spring Data `Page` is kept inside repositories and services and is never serialized directly.

## Stack

- Java 25 (`C:\Logiciels\jdk-25.0.3+9` locally)
- Spring Boot 4.0.6
- Spring WebMVC
- Spring Security JWT resource server
- CORS configuration for frontend apps
- Spring Data JPA / Hibernate 7
- Bean Validation
- Liquibase
- PostgreSQL
- Maven Wrapper

No auditing, cache, actuator, OpenAPI, devtools, notification module, or old JHipster user/authority schema is kept.
