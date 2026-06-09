# AGENTS.md

Agent-facing guide for this repository. Keep this file and `README.md` in sync when architecture, commands, or conventions change.

## Current baseline

- Minimal backend-only Spring Boot CRUD API for HR (`ressources humaines`).
- Java 25 + Spring Boot 4.0.6.
- CORS and JWT authentication are included for the frontend showcase.
- No tests are currently present and no tests should be added unless explicitly requested.
- Auditing and optional JHipster-style infrastructure have been removed.
- Verification used: `./mvnw -DskipTests package` with JDK 25.

## Essential commands

```bash
export JAVA_HOME=/c/Logiciels/jdk-25.0.3+9
export PATH="$JAVA_HOME/bin:$PATH"

./mvnw spring-boot:run
./mvnw -DskipTests compile
./mvnw -DskipTests package
```

Windows scripts:

- `run.bat` sets `JAVA_HOME=C:\Logiciels\jdk-25.0.3+9` and runs Spring Boot.
- `clean.bat` sets the same `JAVA_HOME` and runs `mvn clean package`.
- Maven Wrapper files are present (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`).

## Environment/configuration

- Java 25 is required (`pom.xml` sets `<java.version>25</java.version>`).
- Spring Boot parent: `4.0.6`.
- Main config: `src/main/resources/application.yml`.
- PostgreSQL dev database: `crud_db`; username/password: `crud/crud`.
- Default frontend CORS origins: `http://localhost:3000`, `http://localhost:4200`, `http://localhost:5173`, `http://localhost:9000`.
- Default showcase login is seeded in DB table `app_user`: `admin` / `admin`.
- Server port: `8080`.
- Liquibase changelog entry point: `src/main/resources/liquibase/master.xml`.
- `init.sql` is destructive (`drop schema public cascade; create schema public;`). Do not run it casually.

## Minimal stack

Kept:

- `spring-boot-starter-webmvc`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-liquibase`
- `spring-boot-starter-security`
- `spring-boot-starter-oauth2-resource-server`
- PostgreSQL runtime driver

Removed:

- auditing (`AbstractAuditingEntity`, auditing annotations/config)
- old JHipster database-backed user/authority modules
- cache/Ehcache/Caffeine/JCache
- actuator
- OpenAPI/SpringDoc
- devtools
- notification module
- custom validation framework
- broad custom config/Jackson infrastructure
- JHipster-specific user schema seed data

## Architecture map

```text
app
├── CoreApplication.java
├── core
│   ├── referenceData       # Minimal allow-listed reference data endpoint
│   └── security            # Minimal CORS, database login, JWT issue/validate
└── domain/rh
    ├── conge                # Leave request CRUD, nested under employee for create/list
    ├── departement          # Department CRUD
    ├── employe              # Employee CRUD and Specification-based filtering
    ├── sexe                 # Reference entity/DTO only
    ├── situationFamiliale   # Reference entity/DTO only
    └── typeConge            # Reference entity/DTO only
```

## Security/CORS notes

- `POST /api/authenticate` is public and returns `{ "id_token": "..." }`.
- All other `/api/**` endpoints require `Authorization: Bearer <jwt>`.
- Authentication uses the `app_user` database table seeded by Liquibase.
- Default user is `admin` / `admin`; password hashes are BCrypt.
- JWT signing uses `APP_SECURITY_JWT_BASE64_SECRET`.
- Token TTL uses `APP_SECURITY_TOKEN_VALIDITY_SECONDS`.
- CORS origins use comma-separated `APP_CORS_ALLOWED_ORIGINS`.

## CRUD/domain conventions

- REST controller classes are named `*Resource` and commonly use French method names: `creer`, `maj`, `recupererParId`, `supprimer`, `lister`, `filtrer`.
- Business logic belongs in `*Service`; controllers translate `IllegalArgumentException` to `400`/`404` with `ResponseStatusException`.
- Repositories extend `JpaRepository`; use `JpaSpecificationExecutor` when filter endpoints are needed.
- DTOs are records with static mapping helpers:
  - `toDto(entity)` for full API output.
  - `toDtoAsRef(entity)` for nested lazy references.
  - `toEntity(dto)` and `toEntity(dto, id)` for create/update.
  - `toEntityAsRef(dto)` for associations by id.
- JPA relationships are lazy `@ManyToOne` where applicable.
- Entities normally define `getDisplayString()` and an `id<Entity>()` getter used by DTOs.

## Liquibase conventions

- Never rely on Hibernate DDL generation; `spring.jpa.hibernate.ddl-auto=none`.
- Add one or more XML changelog files under `src/main/resources/liquibase/changelog/`.
- Include new changelog files from `src/main/resources/liquibase/master.xml` in dependency order:
  1. reference/parent tables
  2. child tables
  3. constraints/foreign keys
  4. seed data as appropriate
- Store seed data under `src/main/resources/liquibase/data/*.csv` and reference it with `loadData`.
- Existing domain sequences are named `seq_<table_name>` and usually start at `100`.

## Current REST endpoint inventory

- Auth:
  - `POST /api/authenticate`
- Reference data:
  - `GET /api/reference/{entity}`
  - `GET /api/reference/{entity}/{field}/{value}`
  - `GET /api/reference/{entity}/{id}`
- Departement:
  - `POST /api/departement`
  - `GET /api/departement`
  - `GET /api/departement/{id}`
  - `PUT /api/departement/{id}`
  - `DELETE /api/departement/{id}`
- Employe:
  - `POST /api/employe`
  - `POST /api/employe/filtrer`
  - `GET /api/employe/{id}`
  - `PUT /api/employe/{id}`
  - `DELETE /api/employe/{id}`
- Conge:
  - `POST /api/employe/{idEmploye}/conge`
  - `GET /api/employe/{idEmploye}/conge`
  - `GET /api/conge/{id}`
  - `PUT /api/conge/{id}`
  - `DELETE /api/conge/{id}`

## Watch points for future work

- Existing update services map DTOs to new detached entities. Loading and mutating managed entities would be safer if partial updates are added.
- Liquibase was simplified for a fresh showcase database. Existing databases may need manual cleanup or a reset with `init.sql`.
- Keep user changes safe: run `git status --short` before edits and do not overwrite unrelated modified files.
