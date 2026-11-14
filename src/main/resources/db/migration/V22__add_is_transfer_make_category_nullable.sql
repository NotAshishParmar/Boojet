ALTER TABLE transactions
    ADD COLUMN is_transfer BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE transactions
SET is_transfer = TRUE
WHERE to_account_id IS NOT NULL;

ALTER TABLE transactions
    ALTER COLUMN category_id DROP NOT NULL;