# Minimal Spring Boot 4 modernization notes

Date: 2026-06-09

## User constraints

- Do not add tests.
- Do not stay tied to JHipster.
- Use the latest showcase stack directly.
- Remove auditing and optional components.
- Keep the app minimal, but include CORS and JWT authentication for the separate frontend showcase.
- Local JDK 25 path: `C:\Logiciels\jdk-25.0.3+9`.

## Final target stack

Kept:

- Java 25.
- Spring Boot 4.0.6.
- Spring WebMVC.
- Spring Security + OAuth2 Resource Server JWT validation.
- Minimal database-backed login endpoint for issuing JWTs.
- Minimal CORS configuration.
- Spring Data JPA / Hibernate 7.
- Bean Validation.
- Liquibase 5 through `spring-boot-starter-liquibase`.
- PostgreSQL runtime driver.
- Maven Wrapper.

Removed:

- JHipster-style auditing (`AbstractAuditingEntity`).
- Database-backed user/authority modules and seed data.
- Cache/Ehcache/JCache/Caffeine.
- Actuator.
- SpringDoc/OpenAPI.
- Devtools.
- Notification module.
- Custom validation/assertion framework.
- Custom async Liquibase.
- Broad custom Jackson/date config.
- JHipster user/authority/notification Liquibase schema and seed data.

## Resulting source shape

```text
src/main/java/app
├── CoreApplication.java
├── core
│   ├── referenceData
│   └── security
└── domain/rh
    ├── conge
    ├── departement
    ├── employe
    ├── sexe
    ├── situationFamiliale
    └── typeConge
```

Current compile/package footprint: 32 Java source files.

## Main changes

### Build

- `pom.xml` contains the minimal runtime needs plus frontend security:
  - `spring-boot-starter-webmvc`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-liquibase`
  - `spring-boot-starter-security`
  - `spring-boot-starter-oauth2-resource-server`
  - `postgresql` runtime driver
- Java release remains `25`.
- Spring Boot parent remains `4.0.6`.
- Maven Wrapper is present.

### Configuration

- Reduced to one configuration file: `src/main/resources/application.yml`.
- Kept environment-variable overrides for database, CORS, JWT secret, and token TTL.
- Kept Java virtual threads enabled.

### Security/CORS

- Added `src/main/java/app/core/security/SecurityConfiguration.java`.
- Added `src/main/java/app/core/security/AuthResource.java`.
- `POST /api/authenticate` is public and returns `{ "id_token": "..." }`.
- All other `/api/**` endpoints require `Authorization: Bearer <jwt>`.
- Authentication is backed by the `app_user` database table.
- Default showcase login is seeded by Liquibase as `admin` / `admin` with a BCrypt password hash.
- CORS defaults support localhost frontend ports `3000`, `4200`, `5173`, and `9000`.

### Database/Liquibase

- Removed `_initial_schema.xml` and old JHipster user/authority/notification seed data.
- `liquibase/master.xml` now includes only RH domain tables, seed data, and constraints.
- Master changelog order was simplified: reference tables, child tables, then constraints.

### Auditing

- Removed `AbstractAuditingEntity` and all auditing infrastructure.
- No domain entity uses auditing annotations.

## Verification performed

Using JDK 25:

```bash
export JAVA_HOME=/c/Logiciels/jdk-25.0.3+9
export PATH="$JAVA_HOME/bin:$PATH"

./mvnw -DskipTests package
```

Build completed successfully.

Notes:

- No tests were added.
- Maven/JDK 25 still emits native-access and `Unsafe` warnings from Maven internals; they do not break the build.
- Existing databases with old user/authority/notification tables should be reset for this showcase version, or cleaned manually.
