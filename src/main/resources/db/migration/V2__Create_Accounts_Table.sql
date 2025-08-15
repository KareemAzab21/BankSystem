CREATE TYPE account_type AS ENUM ('SAVINGS', 'CHECKING');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'FROZEN', 'CLOSED');

CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    account_type account_type NOT NULL,
    status account_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_accounts_user_id ON accounts (user_id);
CREATE INDEX idx_accounts_account_number ON accounts (account_number);
