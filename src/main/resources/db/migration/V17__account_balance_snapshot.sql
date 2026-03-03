CREATE TABLE account_balance_snapshot (
  id BIGSERIAL PRIMARY KEY,
  account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
  as_of_date DATE NOT NULL,
  balance_amount NUMERIC(19, 4) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),

  CONSTRAINT uq_account_snapshot UNIQUE (account_id, as_of_date)
);

CREATE INDEX idx_snapshot_account_date
  ON account_balance_snapshot(account_id, as_of_date DESC);