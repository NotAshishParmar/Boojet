-- 1) Income transactions -> INCOME
UPDATE transactions t
SET category_id = c.id
FROM categories c
WHERE t.is_income = TRUE
  AND c.code = 'INCOME'
  AND (t.category_id IS NULL);

-- 2) Expense transactions mapping
UPDATE transactions t
SET category_id = c.id
FROM categories c
WHERE t.is_income = FALSE
  AND t.category_id IS NULL
  AND c.code = CASE t.category
    WHEN 'FOOD' THEN 'FOOD_GROCERIES'
    WHEN 'TRANSPORT' THEN 'TRANSPORT_COMMUTE'
    WHEN 'OTHER' THEN 'OTHER_ESSENTIAL'
    WHEN 'RENT' THEN 'RENT'
    WHEN 'UTILITIES' THEN 'UTILITIES'
    WHEN 'HEALTH' THEN 'HEALTH'
    WHEN 'ENTERTAINMENT' THEN 'ENTERTAINMENT'
    WHEN 'INCOME' THEN 'INCOME' -- just in case
    ELSE NULL
  END;

-- 3) Fail if any transactions still have NULL category_id
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM transactions WHERE category_id IS NULL) THEN
    RAISE EXCEPTION 'Backfill failed: some transactions have NULL category_id.';
  END IF;
END $$;
