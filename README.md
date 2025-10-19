# Boojet - Personal Budgeting Application\
Boojet is a simple personal budgeting application built with Java and Spring Boot. It provides a REST API for managing financial transactions, along with a minimal web UI for basic interaction. Additionally, it includes a console-based CLI prototype for offline use.


## Features

- REST API for budgeting
  - CRUD transactions at `/transactions` (create, list, get by id, update PUT/PATCH, delete)
  - Reports and filters: by category, by year/month, monthly summary, overall balance
- Minimal web UI
  - `src/main/resources/static/boojet.html` for add/edit/delete/filter and live balance
- Rich domain model
  - `Transaction` entity, `Category` enum, and `Money` value object persisted via a JPA `AttributeConverter`
- CLI prototype (offline)
  - Console UI with save modes (AUTO/MANUAL/NONE), JSON persistence to `transactions.json`


## Tech Stack

- Java 17, Maven
- Spring Boot 3.5.x: Web, Data JPA, Validation
- PostgreSQL (via Docker Compose for local dev)
- Jackson (with Java Time module), ModelMapper, Lombok
- JUnit 5
- Static HTML/JavaScript for a lightweight UI


## How to run

API (recommended):

1) Start PostgreSQL

```
docker compose up -d
```

Database config (`src/main/resources/application.properties`):

- `spring.datasource.url=jdbc:postgresql://localhost:5432/postgres`
- `spring.datasource.username=postgres`
- `spring.datasource.password=changemeinprod!`
- `spring.jpa.hibernate.ddl-auto=update`

2) Start the API

```
mvn spring-boot:run
```

3) Use the minimal UI

```
http://localhost:8080/boojet.html
```

Or call endpoints directly (base path `/transactions`).


CLI prototype (manual compile from project root):

```
javac -cp src/main/java BoojetApp.java ConsoleUI.java TransactionManager.java FileStorage.java SaveMode.java
java  -cp .;src/main/java com.boojet.boot_api.BoojetApp
```

Notes:

- CLI files currently live at the project root and reference domain classes under `src/main/java`.
- Data is saved to `transactions.json` in the project root.


## Future Improvements

- API ergonomics: pagination/sorting, query param filters on list endpoints
- Documentation and ops: OpenAPI/Swagger, Actuator health/info
- Validation and errors: bean validation annotations + centralized error handling
- Data layer: aggregate queries in repository instead of in-memory reductions
- Migrations: manage schema with Flyway/Liquibase rather than `ddl-auto`
- Security: Spring Security and CORS configuration
- Frontend: richer UI, charts, budgets/targets (extend `BudgetPeriod`)
- Build/testing: fold CLI into Maven (module/profile); add controller/service tests and re-enable CLI tests

