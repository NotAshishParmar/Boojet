-- Phase 0: Convert transactions.category from ordinal (smallint/int) to string

-- In case a previous attempt left this behind
ALTER TABLE transactions DROP COLUMN IF EXISTS category_tmp;

-- Create temp text column
ALTER TABLE transactions ADD COLUMN category_tmp VARCHAR(64);

-- Map ordinals -> enum names
UPDATE transactions
SET category_tmp = CASE category
  WHEN 0 THEN 'FOOD'
  WHEN 1 THEN 'RENT'
  WHEN 2 THEN 'TRANSPORT'
  WHEN 3 THEN 'ENTERTAINMENT'
  WHEN 4 THEN 'UTILITIES'
  WHEN 5 THEN 'HEALTH'
  WHEN 6 THEN 'OTHER'
  WHEN 7 THEN 'INCOME'
  ELSE NULL
END;

-- Fail migration if something didn't map
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM transactions WHERE category_tmp IS NULL) THEN
    RAISE EXCEPTION 'Category conversion failed: unmapped ordinal values found.';
  END IF;
END $$;

-- Drop the old ordinal column (drops ordinal-related CHECK constraints too)
ALTER TABLE transactions DROP COLUMN category;

-- Rename tmp -> category
ALTER TABLE transactions RENAME COLUMN category_tmp TO category;

-- Enforce not null (if you want)
ALTER TABLE transactions ALTER COLUMN category SET NOT NULL;
