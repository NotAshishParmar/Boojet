ALTER TABLE categories
  DROP CONSTRAINT IF EXISTS categories_type_check;

ALTER TABLE categories
  ADD CONSTRAINT categories_type_check
  CHECK ( (type)::text = ANY (
    (ARRAY[
      'EXPENSE'::character varying,
      'INCOME'::character varying,
      'TRANSFER'::character varying
    ])::text[]
  ));

INSERT INTO categories (
code, name, type,
essential, parent_id,
is_system, user_id,
active, sort_order, created_at
)
SELECT
  'TRANSFER', 'Transfer', 'TRANSFER',
  NULL, NULL,
  true, 1,
  true, 0, now()
WHERE NOT EXISTS (
  SELECT 1
  FROM categories c
  WHERE c.user_id = 1
    AND upper(c.code) = upper('TRANSFER')
);