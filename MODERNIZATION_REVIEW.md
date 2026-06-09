# Minimal Spring Boot 4 modernization notes

Date: 2026-06-09

## Direction

- Solo-developer app: keep it simple and optimized for the owner only.
- Stay on current Spring Boot 4 + Java 25 stack.
- Remove former JHipster/generated style where it does not help the app.
- Keep CORS + JWT because the separate frontend showcase needs them.
- Do not add tests unless explicitly requested.

## Current stack

- Java 25.
- Spring Boot 4.0.6.
- Spring WebMVC.
- Spring Security + OAuth2 Resource Server JWT validation.
- Minimal database-backed login endpoint for issuing JWTs.
- Minimal CORS configuration.
- Spring Data JPA / Hibernate 7.
- Bean Validation.
- Liquibase through `spring-boot-starter-liquibase`.
- PostgreSQL runtime driver.
- Maven Wrapper.

## Removed / avoided

- JHipster-style auditing (`AbstractAuditingEntity`).
- Old database-backed user/authority modules and seed data.
- Cache/Ehcache/JCache/Caffeine.
- Actuator.
- SpringDoc/OpenAPI.
- Devtools.
- Notification module.
- Custom validation/assertion framework.
- Custom async Liquibase.
- Broad custom Jackson/date config.
- Placeholder Maven `settings.xml`.
- Generated entity helpers like fluent `id(...)`, `getDisplayString()`, and `Serializable`.
- Kept `getId<Entity>()` entity getters and matching `id<Entity>` DTO fields because the frontend depends on them.

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

## Latest cleanup pass

### Build/application identity

- Maven artifact/name intentionally stays generic/anonymized: `core` / `app_core`.
- Spring application name intentionally stays `app_core`.
- Main class intentionally stays `CoreApplication` for easy reuse between projects.

### Java/Spring style

- DTO/filter objects use Java records where useful.
- Repositories only declare the Spring Data interfaces they actually need.
- Removed redundant `@Repository` and `@EnableWebSecurity` annotations.
- Removed unused checked exceptions from controllers.
- Update services now load and mutate managed entities instead of saving new detached replacements.
- Missing resources now consistently become `404`, while invalid input remains `400`.
- `SPRING_JPA_SHOW_SQL` now defaults to `false` to keep local logs quieter.

### Liquibase/database

- Liquibase files intentionally stay separated under `src/main/resources/liquibase/changelog/`.
- `src/main/resources/liquibase/master.xml` remains the entry point that includes table and constraint changelogs.
- Seed CSVs remain under `src/main/resources/liquibase/data/`.
- JPA sequence allocation and Liquibase sequence increments are set directly to `1`.

## Verification

Using JDK 25:

```bash
export JAVA_HOME=/c/Logiciels/jdk-25.0.3+9
export PATH="$JAVA_HOME/bin:$PATH"

./mvnw -DskipTests clean package
```

`clean package` completed successfully.
