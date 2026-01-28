ALTER TABLE transactions
  ADD COLUMN IF NOT EXISTS category_id BIGINT NULL REFERENCES categories(id);

CREATE INDEX IF NOT EXISTS idx_transactions_category_id ON transactions(category_id);
