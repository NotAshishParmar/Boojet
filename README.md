# Boojet — Personal Budgeting API

Simple budgeting backend built with Java and Spring Boot. It exposes a REST API to track transactions and a minimal static web page for quick interaction. A small console prototype is also included for offline experimentation.


## Features

- Transactions API
  - CRUD at `/transactions`
  - Filters and reports: by category, by month, monthly summary, overall balance
- Income Planning
  - Create income plans at `/plan`
  - Expected monthly income and net report per month
- Minimal Web UI
  - Static page at `src/main/resources/static/boojet.html` (served at `/boojet.html`)
- Clean Domain Model
  - `Transaction`, `Category`, `Money` (with JPA `AttributeConverter`)


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

API quick peek:

- Transactions base path: `/transactions`
- Income plan base path: `/plan`

Example create transaction:

```
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
        "amount": { "currency": "USD", "amount": 25.50 },
        "category": "FOOD",
        "description": "Lunch"
      }'
```

CLI prototype (optional, from project root):

```
javac -cp src/main/java BoojetApp.java ConsoleUI.java TransactionManager.java FileStorage.java SaveMode.java
java  -cp .;src/main/java com.boojet.boot_api.BoojetApp
```


## Future Improvements

- API ergonomics: pagination/sorting; query params for filters
- Documentation/ops: OpenAPI/Swagger; Actuator health/info
- Validation/errors: bean validation + centralized error handling
- Data layer: more aggregate queries in repositories
- Migrations: Flyway/Liquibase (avoid `ddl-auto` in prod)
- Security: Spring Security and CORS tuning
- Frontend: richer UI and charts; budgets/targets
- Build/testing: fold CLI into Maven; add controller/service tests


## 🧠 Why I Built This
I wanted to build something useful for myself—track my spending, understand where my money goes, and take control of my finances. This is part of my personal journey back into software development.
