-- Allow new writes that only use category_id
ALTER TABLE transactions
  ALTER COLUMN category DROP NOT NULL;
