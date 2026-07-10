# CRUD RH Backend

Minimal Spring Boot REST API for a small HR (`ressources humaines`) CRUD showcase.

Personal/solo-developer project: favor simple, direct, maintainable choices over enterprise or team-oriented process.

The domain CRUD code is generated from a DSL, but the generated output is intended to be a clean, explicit, hand-editable foundation for future AI/manual tweaks. Genericity belongs in the generator/templates, not in the generated runtime code.

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
