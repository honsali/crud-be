# Backend agent notes

- This repository is the runnable Spring Boot backend. Liquibase is authoritative for the physical database schema; do not rewrite executed change sets.
- Bounded SQL strings such as `nvarchar(250)` keep their physical size in Liquibase. Mirror that limit with `@Size(max = 250)` only on independently writable request DTO fields; do not duplicate it with entity-level `@Size` or generated-domain `@Column(length = 250)` metadata.
- Service-level cross-field and business validation uses typed private static `validate(...)` entry points. Bean Validation annotations remain responsible for request-field shape validation.
- Shared reference-data query and route mechanics live in `app.core.referenceData`. Client-specific entity names, labels, and allowed filters belong in a domain-owned `ReferenceDataCatalog` implementation such as `app.domain.rh.referenceData.RhReferenceDataCatalog`.
- Spring Boot's default Problem Details advice can run before an unordered application advice. Field-level request validation is therefore handled by the validation-only, highest-precedence `ValidationExceptionHandler`; do not raise the precedence of the broad `ApiExceptionHandler`, because normal framework 404/405 handling and response headers must remain intact.
- Full-stack E2E orchestration belongs to a separate project that coordinates PostgreSQL, this backend, and the frontend. In this repository, `./mvnw test` currently validates compilation only because there are no test sources.
