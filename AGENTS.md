# Agent instructions

## Project baseline

- This is a Maven/Spring Boot backend that compiles with Java 25.
- The authoritative application version is the top-level `<version>` in `pom.xml`.
- Validate source changes with `./mvnw test`; the repository currently has no test sources, so this gate proves compilation only.
- Preserve unrelated working-tree changes before editing.
- The HR entity/DTO/mapper/repository/service/resource and Liquibase files are generated as an overlay by the sibling `../engine` project, then reviewed and selectively copied here. Security and shared application infrastructure are maintained in this backend.
- Keep generated runtime code explicit and hand-editable. Put reusable generation logic in `../engine`; do not introduce a generic runtime CRUD framework.
- `review_260710.md` is the dated modernization review and implementation checklist.

## Current security contract

- `POST /api/authenticate` is the only public application endpoint. It returns the existing `{ "id_token": "..." }` response contract.
- All other `/api/**` routes require either `ROLE_USER` or `ROLE_ADMIN`. Both roles have equal CRUD access by default.
- Spring method security is enabled. No endpoint is currently ADMIN-only; add a focused `@PreAuthorize("hasRole('ADMIN')")` only when a concrete business rule requires it.
- Only application-owned `ROLE_*` authorities are copied into JWT claims or returned by `/api/user`. Spring Security `FACTOR_*` authorities remain internal and are filtered from JWT claims, decoded application authorities, and public user data.
- JWTs use HS512 and contain configured issuer, audience, timestamps, subject, and application roles.
- `SecurityProperties` binds and validates `application.security`: issuer and audience must be nonblank, and token validity must be between 1 and 604800 seconds.
- An explicit JWT secret must be strict Base64 and decode to at least 64 bytes. Invalid or short explicit secrets fail startup and never fall back.
- Without an explicit secret, an ephemeral random 64-byte key is allowed only when `APP_SECURITY_ALLOW_UNSAFE_DEV_SECRET=true` and the active profile is `dev` or `local`. It is generated once per process, is never logged, and restart invalidates existing local tokens.
- `application-dev.yml` and `application-local.yml` bind the server to `127.0.0.1` by default. `SERVER_ADDRESS` is the deliberate override for Docker or LAN access.
- The Liquibase `dev` context seeds the local `admin/admin` account. Treat it as loopback-only demo access, not production credentials.

## Security configuration variables

- `APP_SECURITY_JWT_BASE64_SECRET`
- `APP_SECURITY_ALLOW_UNSAFE_DEV_SECRET`
- `APP_SECURITY_JWT_ISSUER` (default `app_core`)
- `APP_SECURITY_JWT_AUDIENCE` (default `app_core`)
- `APP_SECURITY_TOKEN_VALIDITY_SECONDS` (default `86400`, allowed `1..604800`)
- `SERVER_ADDRESS` (dev/local default `127.0.0.1`)
- `APP_CORS_ALLOWED_ORIGINS`

## Existing architecture conventions

- Services own transaction boundaries; read operations use `@Transactional(readOnly = true)`.
- `spring.jpa.open-in-view` is disabled and Liquibase owns the schema.
- JPA relationships are lazy; collection queries use entity graphs where needed.
- DTOs are records and dedicated mappers handle full and compact-reference conversion.
- Spring Data repositories do not need `@Repository` annotations.
- Filtering uses `BaseSpecification`; text LIKE values are escaped.
- Pageable endpoints append `id` as a deterministic tie-breaker through `PageableUtils`.
- `ReferenceDataService` uses a strict entity/field allowlist; never interpolate unapproved user-provided JPQL identifiers.
- Unexpected API failures must remain sanitized and logged server-side.
