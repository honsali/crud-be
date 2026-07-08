# Audit feedback — recommendations intentionally not applied now

This file records audit recommendations from `audit.md` and `audit_v2.md` that are intentionally not applied at this stage, with the project-specific reason. These are mostly conscious solo-dev trade-offs, not disagreements with the technical validity of the audit.

## Accepted trade-offs

| Audit point | Current decision | Reason |
|---|---|---|
| Stable page contract with `PageDto` instead of returning `Page<Dto>` | Keep returning Spring Data `Page<Dto>` directly. | The backend and frontend are built by the same solo developer, and Spring is part of the application stack rather than an incidental third-party library. Owning a custom `PageDto` is useful for public/team APIs, but for this project it adds a guardrail we do not currently need. The important pagination fix kept is stable ordering with `id` as fallback/tie-breaker. |
| Generated integration tests | Do not add tests to this application unless explicitly requested. | The repository baseline explicitly says no tests for now. Generated tests remain a good future generator-level investment, but adding them here would change the current lightweight workflow. |
| Role-based authorization (`ROLE_ADMIN`/`ROLE_USER` matchers) | Defer. | Roles are already carried in JWTs and returned to the frontend, but there is no current backend requirement separating read/write permissions. Adding an authorization matrix now would be premature for the showcase. Revisit when the frontend/use case needs different permissions. |
| Dockerfile, compose, `.env.example`, `/health`, deployment kit | Defer. | Useful for client deployment, but this repository is still a local solo-dev backend showcase. Add these when there is an actual deployment target or when packaging becomes a priority. |
| Direct `PageImpl` JSON serialization warning | Accepted for now. | Same rationale as `PageDto`: the solo developer controls both sides. If a future Spring upgrade changes the shape and breaks the frontend, both can be updated together. |

## Kept for compatibility or simplicity

| Audit point | Current decision | Reason |
|---|---|---|
| Duplicate DTO id fields such as `id` and `idEmploye` | Keep. | The frontend depends on these compatibility fields. They are documented as representation-only aliases, and association mapping relies on the normal `id()` field. |
| `rememberMe` accepted by login request but currently ignored | Keep for now. | Removing it could break an existing frontend payload. Implementing a second token TTL is easy later, but not needed until the frontend wants different behavior. |
| English technical error messages | Keep for now. | These messages are currently API/debug details, not a finalized user-facing localization layer. If the frontend displays API `detail` directly to French users, switch messages to French then. |
| `ReferenceDataDto` does not expose `code` | Keep for now. | Current reference-data usage is display/list oriented and only needs `id` + label. Add `code` when the frontend needs code-based conditional logic. |
| `ReferenceDataService` is generic despite the anti-genericity preference | Keep as an intentional small exception. | It is allow-listed, parameterized, and limited to reference reads. It is not a hidden CRUD engine. Generating separate reference endpoints is possible later, but not worth the extra files now. |
| Redundant `try/catch` blocks in resources now that `ApiExceptionHandler` exists | Keep for now. | They are explicit and match the current generated resource convention. The global handler still protects uncaught paths. Removing the catches is a valid future simplification, but not necessary for correctness. |

## Low-risk hardening deferred

| Audit point | Current decision | Reason |
|---|---|---|
| JWT issuer validation on decode | Defer. | Tokens are already signed with the application secret. Issuer validation is a good finishing touch, but the practical risk is low after making the JWT secret required outside explicit dev/local fallback. |
| Random dev JWT secret per generated project | Defer to generator work. | The fallback secret is now allowed only with an explicit unsafe flag and `dev`/`local` Spring profile. Production requires `APP_SECURITY_JWT_BASE64_SECRET`. Per-project random dev secrets are still nice for the generator, but not urgent here. |
| Rate limiting on `/api/authenticate` | Defer. | BCrypt already slows brute-force attempts, and this is not yet an exposed production deployment. Add rate limiting when the app is deployed publicly. |
| `@Version` optimistic locking | Defer. | Last-write-wins is acceptable for the current solo/light CRUD usage. Optimistic locking would require frontend conflict handling and is best added when concurrent editing is a real concern. |
| Creation/update timestamps | Defer. | Auditing was intentionally removed to keep the backend minimal. Add explicit timestamp fields when the domain or client asks for them. |
| Entity graph for single-entity GET endpoints | Defer. | List endpoints already avoid N+1 with entity graphs. A single GET causing a few lazy reference loads is acceptable at this scale. |
| HTTP compression, graceful shutdown, extra operational config | Defer. | These are deployment polish items. Add them when packaging/deployment is part of the workflow. |
| Static OpenAPI generation | Defer to generator work. | Useful for a generator, but not necessary for this backend-only showcase right now. |

## Not applied because out of scope for this repository

| Audit point | Current decision | Reason |
|---|---|---|
| Port all changes back into generator templates | Not done in this repository. | This repo is the generated backend output. Template changes must be made in the generator project/source, not here. The current code and `AGENTS.md` document the desired generated shape. |

## Manual action, not automatic code change

| Audit point | Current decision | Reason |
|---|---|---|
| Reset local database after rewritten Liquibase changesets | Requires explicit manual action. | Existing local databases may hit Liquibase checksum mismatches because pre-release changesets were rewritten. Resetting the DB is destructive, so it should be done deliberately by the owner, e.g. one local run with `SPRING_LIQUIBASE_DROP_FIRST=true` or via the documented destructive `init.sql`. |
