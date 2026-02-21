ALTER TABLE transactions
  ADD COLUMN to_account_id BIGINT NULL;

ALTER TABLE transactions
  ADD CONSTRAINT fk_transactions_to_account
  FOREIGN KEY (to_account_id) REFERENCES accounts(id);