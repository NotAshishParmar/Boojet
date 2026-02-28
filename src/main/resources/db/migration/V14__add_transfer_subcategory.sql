WITH transfer_parent AS (
  SELECT id
  FROM categories
  WHERE user_id = 1 AND code = 'TRANSFER'
  LIMIT 1
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