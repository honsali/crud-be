# CRUD RH Backend

Minimal Spring Boot REST API for a small HR (`ressources humaines`) CRUD showcase.

Personal/solo-developer project: favor simple, direct, maintainable choices over enterprise or team-oriented process.

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

## Project structure

```text
src/main/java/app
├── CoreApplication.java
├── core
│   ├── referenceData       # Minimal allow-listed reference data endpoint
│   └── security            # Minimal CORS + JWT configuration and login endpoint
└── domain/rh
    ├── conge                # Leave requests
    ├── departement          # Departments
    ├── employe              # Employees + filtering
    ├── sexe                 # Gender reference entity/DTO
    ├── situationFamiliale   # Family status reference entity/DTO
    └── typeConge            # Leave-type reference entity/DTO
```

## Configuration

Single config file:

- `src/main/resources/application.yml`

Default local database:

```yaml
spring.datasource.url: jdbc:postgresql://localhost:5432/crud_db
spring.datasource.username: crud
spring.datasource.password: crud
```

Useful environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_SHOW_SQL` default `false`
- `SPRING_LIQUIBASE_ENABLED` default `true`
- `SPRING_LIQUIBASE_DROP_FIRST` default `false`; set to `true` only for a destructive local reset on startup
- `APP_CORS_ALLOWED_ORIGINS` comma-separated, default `http://localhost:3000,http://localhost:4200,http://localhost:5173,http://localhost:9000`
- `APP_SECURITY_JWT_BASE64_SECRET`
- `APP_SECURITY_TOKEN_VALIDITY_SECONDS`

Liquibase runs `classpath:liquibase/master.xml` and loads seed CSVs from `src/main/resources/liquibase/data/`.

> Warning: `init.sql` is destructive; it drops and recreates the PostgreSQL `public` schema.

## Run and build

Windows helper scripts use `JAVA_HOME=C:\Logiciels\jdk-25.0.3+9`:

```bat
run.bat      :: spring-boot:run
clean.bat    :: mvn -DskipTests clean package
```

Portable Maven Wrapper commands:

```bash
export JAVA_HOME=/c/Logiciels/jdk-25.0.3+9
export PATH="$JAVA_HOME/bin:$PATH"

./mvnw spring-boot:run
./mvnw -DskipTests compile
./mvnw -DskipTests package
```

Application port: `8080`.

## Authentication

Login endpoint uses the `app_user` database table seeded by Liquibase. Default showcase user is `admin` / `admin`.

```http
POST /api/authenticate
Content-Type: application/json

{"username":"admin","password":"admin"}
```

Response:

```json
{"id_token":"<jwt>"}
```

All `/api/**` endpoints except `/api/authenticate` require:

```http
Authorization: Bearer <jwt>
```

Current user info for the frontend:

```http
GET /api/user
Authorization: Bearer <jwt>
```

Returns fields including `username`, `role`, `roles`, and `authorities`.

## API overview

### User

- `GET /api/user`

### Departments

- `POST /api/departement`
- `GET /api/departement`
- `GET /api/departement/{id}`
- `PUT /api/departement/{id}`
- `DELETE /api/departement/{id}`

### Employees

- `POST /api/employe`
- `POST /api/employe/filtrer` with pageable query parameters
- `GET /api/employe/{id}`
- `PUT /api/employe/{id}`
- `DELETE /api/employe/{id}`

### Employee leave (`conge`)

- `POST /api/employe/{idEmploye}/conge`
- `GET /api/conge/employe/{idEmploye}`
- `GET /api/conge/{id}`
- `PUT /api/conge/{id}`
- `DELETE /api/conge/{id}`

### Reference data

- `GET /api/reference/{entity}`
- `GET /api/reference/{entity}/{field}/{value}`
- `GET /api/reference/{entity}/{id}`

Allowed reference entities include: `departement`, `employe`, `sexe`, `situationFamiliale`, `typeConge`.

## Development notes

- Domain controllers are named `*Resource`; services are named `*Service`; repositories extend Spring Data JPA without `@Repository` on repository interfaces.
- Controller methods declare the full endpoint path directly on their mapping annotation instead of relying on class-level `@RequestMapping` prefixes.
- Filtering by another entity field stays under the current resource path, for example `GET /api/conge/employe/{idEmploye}`.
- Create/update endpoints store the service response in a local variable named `result` before returning it, for easier debugging.
- CRUD resources map `IllegalArgumentException` to `400` and `NoSuchElementException` to `404`; `creer` endpoints may keep the `NoSuchElementException` catch consistently even before references exist.
- Normal CRUD services use DTO mapping helpers for entity/DTO conversion instead of direct `EntityManager` use.
- DTOs are Java records and provide small mapping helpers such as `toDto`, `toDtoAsRef`, `toEntity`, `toEntityAsRef`, and `copyToEntity` where useful.
- API DTOs keep both `id` and compatibility fields like `idDepartement` / `idEmploye` because the frontend uses them.
- Entity getters use explicit `return this.field;` style, and entities do not implement `Serializable` unless a real serialization need appears.
- Relationships use lazy `@ManyToOne` associations and reference DTOs to avoid deep object graphs.
- Schema is managed by separated Liquibase files under `src/main/resources/liquibase/changelog/`, included from `src/main/resources/liquibase/master.xml`.
- Keep development workflow lightweight; avoid abstractions/tooling that only serve future teams or organizational conventions.
- Maven versions intentionally avoid `-SNAPSHOT` for this solo project.
- No tests are included by request.
