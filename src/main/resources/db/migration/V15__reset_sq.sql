-- One-time reset: delete all app data, keep schema + flyway history
TRUNCATE TABLE
  transactions,
  income_plans,
  accounts,
  categories,
  users
RESTART IDENTITY
CASCADE;