CREATE TABLE IF NOT EXISTS categories (
  id           BIGSERIAL PRIMARY KEY,
  code         VARCHAR(64)  NOT NULL UNIQUE,
  name         VARCHAR(64)  NOT NULL,
  type         VARCHAR(16)  NOT NULL CHECK (type IN ('EXPENSE','INCOME')),
  essential    BOOLEAN NULL,
  parent_id    BIGINT NULL REFERENCES categories(id),
  is_system    BOOLEAN NOT NULL DEFAULT TRUE,
  owner_user_id BIGINT NULL,
  active       BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order   INT NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_id);
