# Boojet — Personal Budgeting API

Budgeting API built with Java and Spring Boot. It exposes a REST API to track and record transactions for multiple accounts and display statistics based on the input data. Allows the user to define plans based on their expected income. Also includes a minimal static web page for quick interaction. 


## Features

- Transactions API
  - CRUD operations at `/transactions`
  - Filter by date range, category, income/expense
  - Pagination support
- Accounts: 
    - Group transactions under different accounts
    - List transactions per account at `/accounts/{id}/transactions`
- Categories
  - Predefined set of categories (FOOD, RENT, UTILITIES, etc.)
  - Assign categories to transactions
- Income Planning
  - Define monthly income plans at `/plan`
  - Compare actual income vs planned
- Simple Static UI
  - Basic HTML/JS page at `/boojet.html` for quick interaction
- Data Persistence
  - PostgreSQL database (Docker Compose for local dev)


## Tech Stack

- Java 17, Maven
- Spring Boot 3.5.x (Web, Data JPA, Validation)
- PostgreSQL (Docker Compose for local dev)
- ModelMapper, Lombok, Jackson
- JUnit 5

## How To Run

Prerequisites:

- Java 17+
- Maven 3.9+
- Docker (optional, for local PostgreSQL)

1) Start PostgreSQL (optional but recommended)

```
docker compose up -d
```

The default configuration (`src/main/resources/application.properties`):

- `spring.datasource.url=jdbc:postgresql://localhost:5432/postgres`
- `spring.datasource.username=postgres`
- `spring.datasource.password=changemeinprod!`
- `spring.jpa.hibernate.ddl-auto=update`

2) Start the API

```
mvn spring-boot:run
```

3) Open the simple UI

```
http://localhost:8080/boojet.html
```

## Screenshots

- **Accounts View:**
![Boojet UI Screenshot](docs/res/Accounts.png)

- **Transactions View:**
![Boojet UI Screenshot](docs/res/Transactions.png)

- **Income Planning View:**
![Boojet UI Screenshot](docs/res/IncomePlans_and_Summary.png)


## Docs

- Architecture Overview: [Architecture Diagram](docs/architecture.md)
- API Docs: [API Documentation](docs/api-docs.md)
- Testing Strategy: [Testing Documentation](docs/testing.md)


## Future Improvements

- API ergonomics: ~~pagination~~/sorting; query params for filters
- Documentation/ops: ~~OpenAPI/Swagger~~; Actuator health/info
- Validation/errors: bean validation + ~~centralized error handling~~
- Data layer: ~~more aggregate queries in repositories~~
- Migrations: Flyway/Liquibase (avoid `ddl-auto` in prod)
- Security: Spring Security and CORS tuning
- Frontend: richer UI and charts; budgets/targets
- Build/testing: fold CLI into Maven; add controller/service tests


## Why I Built This
I wanted to build something useful for myself. Boojet helps me track my spending, understand where my money goes, and take control of my finances. This is part of my personal journey back into software development.
