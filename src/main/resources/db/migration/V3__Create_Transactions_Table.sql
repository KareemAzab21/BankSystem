CREATE TYPE transaction_type AS ENUM ('TRANSFER', 'DEPOSIT', 'WITHDRAWAL');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED');

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(36) NOT NULL UNIQUE,
    source_account_id BIGINT,
    destination_account_id BIGINT,
    amount DECIMAL(19, 2) NOT NULL,
    type transaction_type NOT NULL,
    status transaction_status NOT NULL,
    description VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_account_id) REFERENCES accounts (id),
    FOREIGN KEY (destination_account_id) REFERENCES accounts (id),
    CHECK (amount > 0)
);

CREATE INDEX idx_transactions_source_account ON transactions (source_account_id);
CREATE INDEX idx_transactions_destination_account ON transactions (destination_account_id);
CREATE INDEX idx_transactions_timestamp ON transactions (timestamp);
