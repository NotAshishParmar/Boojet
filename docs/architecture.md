# Architecture Overview
 
 Boojet is a layered Spring Boot app: **Controllers -> Services -> Repositories -> PostgreSQL Database**, with a simple static web UI to access the API.

## Layers
- **UI:** A simple static HTML/JS interface to interact with the API.
- **Controllers:** Handle HTTP requests, map endpoints to service methods, and return responses.
- **Services:** Contain business logic, process data, and interact with repositories.
- **Repositories:** Interface with the PostgreSQL database using Spring Data JPA.
- **Database:** PostgreSQL stores accounts, transactions, and categories.


```mermaid
flowchart LR
  %% Groups
  subgraph Frontend
    UI[UI: boojet.html + boojet.js]
  end

  subgraph Backend
    C[REST Controllers]
    S[Services]
    R[JPA Repositories]
    DB[(PostgreSQL)]
  end

  %% Flow
  UI -->|HTTP/JSON| C
  C -->|Service calls| S
  S -->|DB queries| R
  R -->|Persistence| DB

  DB -. Query results .-> R
  R  -. Data objects  .-> S
  S  -. Processed DTO .-> C
  C -->|HTTP response| UI

``` 

## Domain Model

```mermaid
classDiagram
  direction LR
  hide empty members

  %% ================== Domains ==================
  namespace Domain {
    class Transaction {
      -Long id
      -String description
      -Money amount
      -LocalDate date
      -Category category
      -boolean income
      -Account account
    }

    class Account {
      -Long id
      -User user
      -String name
      -AccountType type
      -Money openingBalance
      -LocalDate createdAt
      -LocalDate closedAt
    }

    class IncomePlan {
      -Long id
      -User user
      -String sourceName
      -PayType payType
      -Money amount
      -BigDecimal hoursPerWeek
      -LocalDate effectiveFrom
      -LocalDate effectiveTo
      +Money calculateMonthlyAmount(YearMonth)
      +boolean activeIn(YearMonth)
    }

    class Money {
      -BigDecimal amount
      +static Money of(BigDecimal)
      +BigDecimal asBigDecimal()
      +Money add(Money)
      +Money subtract(Money)
      +Money negate()
    }

    class User {
      -Long id
      -String username
    }
  }

  %% ================== Enums ==================
  namespace Enums {
    class Category {
        <<enumeration>>
    FOOD
    RENT
    TRANSPORT
    ENTERTAINMENT
    UTILITIES
    HEALTH
    OTHER
    }

    class AccountType {
        <<enumeration>>
    CHEQUING
    SAVINGS
    CREDIT_CARD
    CASH
    OTHER
    }

    class PayType {
        <<enumeration>>
    HOURLY
    WEEKLY
    BIWEEKLY
    MONTHLY
    ANNUAL
    }
  }

  %% ================== Infrastructure ==================
  namespace Infra {
    class MoneyConverter {
      +BigDecimal toDatabaseColumn(Money)
      +Money toEntityAttribute(BigDecimal)
    }
  }

  %% ================== Relationships ==================
  Transaction "many" --> "1" Account : belongs to
  Account    "many" --> "1" User
  IncomePlan "many" --> "1" User

  Transaction ..> Money
  Account ..> Money
  IncomePlan ..> Money

  Transaction ..> Category
  Account ..> AccountType
  IncomePlan ..> PayType

  %% Notes
  note for IncomePlan "hoursPerWeek is required when payType = HOURLY"


```


