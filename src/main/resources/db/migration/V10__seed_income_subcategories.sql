WITH income_parent AS (
  SELECT id
  FROM categories
  WHERE user_id = 1 AND code = 'INCOME'
  LIMIT 1
)
INSERT INTO categories (code, name, type, essential, parent_id, is_system, user_id, active, sort_order, created_at)
SELECT v.code, v.name, 'INCOME', NULL, p.id, true, 1, true, v.sort_order, now()
FROM income_parent p
JOIN (VALUES
  ('INCOME_PAY',       'Work Pay',      10),
  ('INCOME_REFUND',    'Refund',        20),
  ('INCOME_ETRANSFER', 'E-Transfer In', 30),
  ('INCOME_GIFT',      'Gift',          40),
  ('INCOME_INTEREST',  'Interest',      50),
  ('INCOME_OTHER',     'Other Income',  90)
) AS v(code, name, sort_order) ON true
WHERE NOT EXISTS (
  SELECT 1 FROM categories c
  WHERE c.user_id = 1 AND upper(c.code) = upper(v.code)
);