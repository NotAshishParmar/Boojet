# Boojet API — Quick Reference

Base URL (local dev): `http://localhost:8080`

- Interactive docs: **/swagger-ui.html**  
- OpenAPI JSON: **/v3/api-docs**
- Auth: None (dev only)



## Transactions

**Model (TransactionDto)**
`id, description, amount, date, category, income (boolean), account { id, name }`

### Search (paged)

- `GET /transactions`

Query params (all optional):
  `page, size, accountId, category, year, month`

**Example**

  ```bash
  curl "http://localhost:8080/transactions?page=0&size=20&accountId=1&year=2026&month=1"
  ```

### Create

- `POST /transactions`

```json
{
  "description": "Lunch",
  "amount": 13.50,
  "date": "2026-01-08",
  "category": "FOOD",
  "income": false,
  "account": { "id": 1 }
}
```

### Read / Update / Patch / Delete

- `GET    /transactions/{id}`
- `PUT    /transactions/{id}`
- `PATCH  /transactions/{id}`
- `DELETE /transactions/{id}`

### Quick filters (simple lists)

- `GET /transactions/category/{cat}`
- `GET /transactions/month/{year}/{month}`
- `GET /transactions/summary/{year}/{month}` → per-category totals
- `GET /transactions/balance` → overall balance (number)



## Accounts


**Model (Account)**
`id, user {id}, name, type, openingBalance, createdAt, closedAt`

### CRUD

- `GET    /account` (all)
- `POST   /account`
- `GET    /account/{id}`
- `PUT    /account/{id}`
- `PATCH  /account/{id}`
- `DELETE /account/{id}`

### Extras

- `GET /account/balance/{id}` → current balance for an account
- `GET /account/{id}/transactions` → all transactions for an account (ordered by date desc)

**Create example**

```bash
curl -X POST http://localhost:8080/account \
  -H "Content-Type: application/json" \
  -d '{
        "user": {"id": 1},
        "name": "Main",
        "type": "CHEQUING",
        "openingBalance": 0
      }'
```


## Income Plans

**Model (IncomePlan)**
`id, user {id}, sourceName, payType, amount, hoursPerWeek?, effectiveFrom, effectiveTo?`

### CRUD

- `GET    /plan`
- `POST   /plan`
- `GET    /plan/{id}`
- `PUT    /plan/{id}`
- `PATCH  /plan/{id}`
- `DELETE /plan/{id}`

### Reports

- `GET /plan/expected/{year}/{month}` → expected income for month (number)
- `GET /plan/net/{year}/{month}` → **NetReport**

  ```json
  {
    "expectedIncome": 4400.00,
    "actualIncome": 4300.00,
    "expenses": 3100.00,
    "netExpected": 1300.00,
    "netActual": 1200.00
  }
  ```


## Enums

- **Category:** `FOOD, RENT, TRANSPORT, ENTERTAINMENT, UTILITIES, HEALTH, OTHER`
- **AccountType:** `CHEQUING, SAVINGS, CREDIT_CARD, CASH, OTHER`
- **PayType:** `HOURLY, WEEKLY, BIWEEKLY, MONTHLY, ANNUAL`


## Errors

- `400 Bad Request` – invalid input or enum
- `404 Not Found` – resource missing
- `409 Conflict` – uniqueness violations (e.g., duplicate account name per user)

