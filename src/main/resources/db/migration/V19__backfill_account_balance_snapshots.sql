INSERT INTO account_balance_snapshots (account_id, as_of_date, balance_amount, created_at)
SELECT a.id,
       a.created_at,
       a.opening_balance,
       NOW()
FROM accounts a
WHERE NOT EXISTS (
  SELECT 1
  FROM account_balance_snapshots s
  WHERE s.account_id = a.id
);