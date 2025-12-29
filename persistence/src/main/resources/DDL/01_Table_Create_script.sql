-- Drop in dependency order
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS operation_types;
DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
    account_id     BIGSERIAL PRIMARY KEY,
    document_number VARCHAR(32) NOT NULL UNIQUE,
    created_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE operation_types (
    operation_id     BIGSERIAL PRIMARY KEY,
    description      TEXT NOT NULL,
    transaction_type VARCHAR(32) NOT NULL,
    handler          VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE transactions (
    transaction_id    BIGSERIAL PRIMARY KEY,
    account_id        BIGINT NOT NULL REFERENCES accounts(account_id) ON DELETE RESTRICT,
    operation_type_id BIGINT NOT NULL REFERENCES operation_types(operation_id) ON DELETE RESTRICT,
    amount            NUMERIC(12,2) NOT NULL,
    event_date        TIMESTAMP(6) NOT NULL,
    created_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_tx_account_date ON transactions (account_id, event_date);
CREATE INDEX idx_tx_account_operation ON transactions (account_id, operation_type_id);
CREATE INDEX idx_acct_active ON accounts (account_id,is_active);