-- Re-seed system categories for user_id = 1
-- Assumes user_id=1 exists

-- ROOTS
INSERT INTO categories (code, name, type, essential, parent_id, is_system, user_id, active, sort_order, created_at)
SELECT v.code, v.name, v.type, NULL, NULL, true, 1, true, v.sort_order, now()
FROM (VALUES
  ('FOOD',          'Food',          'EXPENSE', 10),
  ('TRANSPORT',     'Transport',     'EXPENSE', 20),
  ('OTHER',         'Other',         'EXPENSE', 90),
  ('RENT',          'Rent',          'EXPENSE', 30),
  ('UTILITIES',     'Utilities',     'EXPENSE', 40),
  ('HEALTH',        'Health',        'EXPENSE', 50),
  ('ENTERTAINMENT', 'Entertainment', 'EXPENSE', 60),
  ('INCOME',        'Income',        'INCOME',   5),
  ('TRANSFER',      'Transfer',      'TRANSFER', 0)
) AS v(code, name, type, sort_order)
WHERE NOT EXISTS (
  SELECT 1 FROM categories c
  WHERE c.user_id = 1 AND upper(c.code) = upper(v.code)
);

-- CHILDREN (INCOME)
WITH income_parent AS (
  SELECT id FROM categories WHERE user_id = 1 AND code = 'INCOME' LIMIT 1
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

-- CHILDREN (FOOD)
WITH food_parent AS (
  SELECT id FROM categories WHERE user_id = 1 AND code = 'FOOD' LIMIT 1
)
INSERT INTO categories (code, name, type, essential, parent_id, is_system, user_id, active, sort_order, created_at)
SELECT v.code, v.name, 'EXPENSE', v.essential, p.id, true, 1, true, v.sort_order, now()
FROM food_parent p
JOIN (VALUES
  ('FOOD_GROCERIES',   'Groceries',  true,  11),
  ('FOOD_DINING_OUT',  'Dining Out', false, 12)
) AS v(code, name, essential, sort_order) ON true
WHERE NOT EXISTS (
  SELECT 1 FROM categories c
  WHERE c.user_id = 1 AND upper(c.code) = upper(v.code)
);

-- CHILDREN (TRANSPORT)
WITH transport_parent AS (
  SELECT id FROM categories WHERE user_id = 1 AND code = 'TRANSPORT' LIMIT 1
)
INSERT INTO categories (code, name, type, essential, parent_id, is_system, user_id, active, sort_order, created_at)
SELECT v.code, v.name, 'EXPENSE', v.essential, p.id, true, 1, true, v.sort_order, now()
FROM transport_parent p
JOIN (VALUES
  ('TRANSPORT_COMMUTE',  'Gas/Transit', true,  21),
  ('TRANSPORT_RIDESHARE','Ride-share',  false, 22)
) AS v(code, name, essential, sort_order) ON true
WHERE NOT EXISTS (
  SELECT 1 FROM categories c
  WHERE c.user_id = 1 AND upper(c.code) = upper(v.code)
);

-- CHILDREN (OTHER)
WITH other_parent AS (
  SELECT id FROM categories WHERE user_id = 1 AND code = 'OTHER' LIMIT 1
)
INSERT INTO categories (code, name, type, essential, parent_id, is_system, user_id, active, sort_order, created_at)
SELECT v.code, v.name, 'EXPENSE', v.essential, p.id, true, 1, true, v.sort_order, now()
FROM other_parent p
JOIN (VALUES
  ('OTHER_ESSENTIAL',    'Misc Essential',     true,  91),
  ('OTHER_NONESSENTIAL', 'Misc Non-essential', false, 92)
) AS v(code, name, essential, sort_order) ON true
WHERE NOT EXISTS (
  SELECT 1 FROM categories c
  WHERE c.user_id = 1 AND upper(c.code) = upper(v.code)
);

-- CHILDREN (TRANSFER) - leaf for your modal rule
WITH transfer_parent AS (
  SELECT id FROM categories WHERE user_id = 1 AND code = 'TRANSFER' LIMIT 1
)
INSERT INTO categories (code, name, type, essential, parent_id, is_system, user_id, active, sort_order, created_at)
SELECT v.code, v.name, 'TRANSFER', NULL, p.id, true, 1, true, v.sort_order, now()
FROM transfer_parent p
JOIN (VALUES
  ('TRANSFER_MOVE', 'Transfer', 10)
) AS v(code, name, sort_order) ON true
WHERE NOT EXISTS (
  SELECT 1 FROM categories c
  WHERE c.user_id = 1 AND upper(c.code) = upper(v.code)
);