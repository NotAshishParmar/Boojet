# Architecture Overview
 
 Boojet is a layered Spring Boot app: **Controllers -> Services -> Repositories -> PostgreSQL Database**, with a simple static web UI to access the API.

## Layers
- **UI:** A simple static HTML/JS interface to interact with the API.
- **Controllers:** Handle HTTP requests, map endpoints to service methods, and return responses.
- **Services:** Contain business logic, process data, and interact with repositories.
- **Repositories:** Interface with the PostgreSQL database using Spring Data JPA.
- **Database:** PostgreSQL stores users, accounts, transactions, and income plans. Enums (Category, AccountType, PayType) are stored as strings.


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
  direction TB
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

  %% ================== Relationships (domain only) ==================
  Transaction "many" --> "1" Account : belongs to
  Account    "many" --> "1" User
  IncomePlan "many" --> "1" User

  Transaction ..> Money
  Account ..> Money
  IncomePlan ..> Money
```

```mermaid
    classDiagram
  direction TB
  hide empty members

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

```

## Request Flow

```mermaid
sequenceDiagram
  autonumber
  participant UI as Browser
  participant C as TransactionController
  participant S as TransactionService
  participant R as TransactionRepository
  participant DB as Postgres

  UI->>C: GET /transactions?page=0&size=20&accountId=1&year=2026&month=1
  C->>S: search(filters, pageable)
  S->>S: validate accountId, compute from/to
  S->>R: search(accountId, category, from, to, pageable)
  R->>DB: SELECT ... ORDER BY date DESC LIMIT/OFFSET
  DB-->>R: Page<Transaction>
  R-->>S: Page<Transaction>
  S-->>C: PageResponse<TransactionDto>
  C-->>UI: JSON

```

## Persistence Model

### Tables

```mermaid


erDiagram
    TRANSACTION {
        BIGINT id PK
        VARCHAR description
        NUMERIC amount
        DATE date
        VARCHAR category
        BOOLEAN is_income
        BIGINT account_id FK
    }

    ACCOUNT {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR name
        VARCHAR type
        NUMERIC opening_balance
        DATE created_at
        DATE closed_at
    }

    INCOME_PLAN {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR source_name
        VARCHAR pay_type
        NUMERIC amount
        DECIMAL hours_per_week
        DATE effective_from
        DATE effective_to
    }

    USER {
        BIGINT id PK
        VARCHAR username
    }

    TRANSACTION }o--|| ACCOUNT : belongs_to
    ACCOUNT }o--|| USER : owned_by
    INCOME_PLAN }o--|| USER : belongs_to
```

### Indexes
Boojet uses a few small, targeted indexes to keep the most common reads fast (paging by account + date; preventing duplicate account names per user).

| Table          | Index name                 | Columns / Type               | Purpose                                                                     |
| -------------- | -------------------------- | ---------------------------- | --------------------------------------------------------------------------- |
| `transactions` | `transactions_pkey`        | `id` (btree, PK)             | Primary key.                                                                |
| `transactions` | `idx_tx_account_date_desc` | `(account_id, date DESC)`    | Speeds up paged lists like: filter by account and month, order by date desc.   |
| `accounts`     | `accounts_pkey`            | `id` (btree, PK)             | Primary key.                                                                |
| `accounts`     | `uq_accounts_user_name`    | `(user_id, name)` **UNIQUE** | One account name per user; prevents duplicates (e.g., two “Main” accounts). |
| `users`        | `users_pkey`               | `id` (btree, PK)             | Primary key.                                                                |
| `income_plans` | `income_plans_pkey`        | `id` (btree, PK)             | Primary key.                                                                |
