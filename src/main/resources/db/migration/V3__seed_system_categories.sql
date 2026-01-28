-- Parents
INSERT INTO categories (code, name, type, essential, parent_id, is_system, sort_order)
VALUES
  ('FOOD', 'Food', 'EXPENSE', NULL, NULL, TRUE, 10),
  ('TRANSPORT', 'Transport', 'EXPENSE', NULL, NULL, TRUE, 20),
  ('OTHER', 'Other', 'EXPENSE', NULL, NULL, TRUE, 90)
ON CONFLICT (code) DO NOTHING;

-- Standalone leaf categories
INSERT INTO categories (code, name, type, essential, parent_id, is_system, sort_order)
VALUES
  ('RENT', 'Rent', 'EXPENSE', TRUE, NULL, TRUE, 30),
  ('UTILITIES', 'Utilities', 'EXPENSE', TRUE, NULL, TRUE, 40),
  ('HEALTH', 'Health', 'EXPENSE', TRUE, NULL, TRUE, 50),
  ('ENTERTAINMENT', 'Entertainment', 'EXPENSE', FALSE, NULL, TRUE, 60),
  ('INCOME', 'Income', 'INCOME', NULL, NULL, TRUE, 5)
ON CONFLICT (code) DO NOTHING;

-- Food children
INSERT INTO categories (code, name, type, essential, parent_id, is_system, sort_order)
SELECT 'FOOD_GROCERIES', 'Groceries', 'EXPENSE', TRUE, id, TRUE, 11
FROM categories WHERE code='FOOD'
ON CONFLICT (code) DO NOTHING;

INSERT INTO categories (code, name, type, essential, parent_id, is_system, sort_order)
SELECT 'FOOD_DINING_OUT', 'Dining Out', 'EXPENSE', FALSE, id, TRUE, 12
FROM categories WHERE code='FOOD'
ON CONFLICT (code) DO NOTHING;

-- Transport children
INSERT INTO categories (code, name, type, essential, parent_id, is_system, sort_order)
SELECT 'TRANSPORT_COMMUTE', 'Gas/Transit', 'EXPENSE', TRUE, id, TRUE, 21
FROM categories WHERE code='TRANSPORT'
ON CONFLICT (code) DO NOTHING;

INSERT INTO categories (code, name, type, essential, parent_id, is_system, sort_order)
SELECT 'TRANSPORT_RIDESHARE', 'Ride-share', 'EXPENSE', FALSE, id, TRUE, 22
FROM categories WHERE code='TRANSPORT'
ON CONFLICT (code) DO NOTHING;

-- Other children
INSERT INTO categories (code, name, type, essential, parent_id, is_system, sort_order)
SELECT 'OTHER_ESSENTIAL', 'Misc Essential', 'EXPENSE', TRUE, id, TRUE, 91
FROM categories WHERE code='OTHER'
ON CONFLICT (code) DO NOTHING;

INSERT INTO categories (code, name, type, essential, parent_id, is_system, sort_order)
SELECT 'OTHER_NONESSENTIAL', 'Misc Non-essential', 'EXPENSE', FALSE, id, TRUE, 92
FROM categories WHERE code='OTHER'
ON CONFLICT (code) DO NOTHING;
