# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.2.4 REST API backend application built with Java 21, using PostgreSQL database and Liquibase for schema management. The application follows a domain-driven design approach with HR (ressources humaines) management functionality.

## Core Technologies

- **Framework**: Spring Boot 3.2.4 with Spring Security, Spring Data JPA
- **Java Version**: 21
- **Database**: PostgreSQL (default), SQL Server support available
- **ORM**: Hibernate 6 with Ehcache (L2 caching enabled)
- **Schema Migration**: Liquibase
- **Authentication**: JWT tokens with custom security configuration
- **API Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven

## Development Commands

### Building and Running
- **Run application**: `run.bat` (sets Java 21 path and runs with dev profile)
- **Clean build**: `clean.bat` (Maven clean install, skipping tests)
- **Maven run**: `mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev`
- **Test**: `mvn test` (if tests are implemented)

### Database Setup
The application expects a PostgreSQL database:
- Database: `crud_db`
- Username/Password: `crud/crud`
- URL: `jdbc:postgresql://localhost:5432/crud_db`
- Schema managed via Liquibase migrations in `src/main/resources/liquibase/`

## Architecture

### Package Structure
```
app/
├── core/                    # Core infrastructure components
│   ├── auth/               # JWT authentication system
│   ├── cache/              # Ehcache configuration
│   ├── config/             # Application configuration
│   ├── cors/               # CORS configuration
│   ├── database/           # Database configuration & base entities
│   ├── liquibase/          # Liquibase configuration
│   ├── security/           # Security configuration & JWT filter
│   ├── user/               # User management & authorities
│   └── validation/         # Custom validation framework
└── domain/
    └── rh/                 # HR domain (ressources humaines)
        ├── employe/        # Employee management
        ├── departement/    # Department management
        ├── sexe/           # Gender reference data
        └── situationFamiliale/  # Family situation reference data
```

### Domain Architecture
- **Entity-Repository-Resource Pattern**: Each domain follows JPA entity, Spring Data repository, and REST controller structure
- **DTO Pattern**: Entities have corresponding DTOs for API responses (e.g., `EmployeDto`)
- **Lazy Loading**: Entities use `@ManyToOne(fetch = FetchType.LAZY)` for relationships
- **Caching**: All entities use `@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)`
- **Sequence Generators**: PostgreSQL sequences used for ID generation

### Database Schema Management
- **Liquibase**: All schema changes managed via XML changelogs
- **File Structure**: Separate files for tables, constraints, and data
- **Master File**: `liquibase/master.xml` includes all change sets in order
- **Data Loading**: CSV files in `liquibase/data/` for reference data

### Security Features
- **JWT Authentication**: Base64-encoded secret in application.properties
- **CORS Configuration**: Supports multiple origins including localhost and production domains
- **Content Security Policy**: Configured for web security
- **Authority-Based Access**: User-authority relationship managed in core.user package

### Validation Framework
Custom validation system in `core.validation` package:
- `Assert` class for validation logic
- Multiple specific exception types for different validation scenarios
- Assertion-based validation approach

## Configuration Notes

### Application Properties
- **Profile**: Application runs with `dev` profile by default
- **Database**: Configured for PostgreSQL with Hikari connection pool
- **JPA**: Shows SQL queries, uses Hibernate statistics disabled
- **Caching**: 1-hour TTL, max 100 entries
- **JWT**: 1-day token validity, 365-day remember-me tokens

### CORS Configuration
- Supports localhost development and production domains
- Allows all methods and headers for development
- Custom exposed headers for application-specific responses

## Development Guidelines

### When Adding New Domain Entities
1. Create entity class with JPA annotations and caching
2. Implement corresponding DTO with `toDto()` static method
3. Create Spring Data repository with custom query methods
4. Implement REST controller following existing patterns
5. Add Liquibase changesets for table and constraints
6. Include CSV data if reference data is needed

### Database Changes
- All schema changes must use Liquibase XML changesets
- Update `master.xml` to include new changesets
- Use separate files for tables and constraints
- Follow existing naming conventions for sequences and tables

### Code Patterns
- Use lazy loading for entity relationships
- Implement custom repository methods for complex queries (e.g., `filtrer()`)
- Follow REST naming: POST for creation, PUT for updates, GET for retrieval
- Use ResponseEntity for proper HTTP status handling
- Implement proper error handling with ResponseStatusException