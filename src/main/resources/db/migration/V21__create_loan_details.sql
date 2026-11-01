CREATE TABLE loan_details (
    id                  BIGSERIAL PRIMARY KEY,
    account_id          BIGINT NOT NULL UNIQUE REFERENCES accounts(id),
    original_principal  NUMERIC(19,2) NOT NULL,
    annual_interest_rate NUMERIC (8,4) NOT NULL,
    term_months         INTEGER NOT NULL
);