-- Ensure categories.user_id is always set
ALTER TABLE categories
ALTER COLUMN user_id SET NOT NULL;