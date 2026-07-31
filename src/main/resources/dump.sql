-- User Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE ,
    gender VARCHAR(20),
    date_of_birth DATE NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Customers Table
CREATE TABLE customers(
    id UUID PRIMARY KEY,
    address VARCHAR(255) NOT NULL,
    nin VARCHAR(11) NOT NULL UNIQUE,
    bvn VARCHAR(11) NOT NULL UNIQUE,

    CONSTRAINT fk_customer_user
        FOREIGN KEY (id)
            REFERENCES users(id) ON DELETE CASCADE
);

-- Accounts table
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL UNIQUE,
    account_status VARCHAR(20) NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    balance NUMERIC(19,2) NOT NULL,
    account_tier VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_account_owner
        FOREIGN KEY (owner_id)
            REFERENCES users(id)
);

-- Transaction Table

CREATE TABLE transactions(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_type VARCHAR(20) NOT NULL,
    transaction_status VARCHAR(20) NOT NULL,
    source_account VARCHAR(20),
    destination_account VARCHAR(20) NOT NULL,
    amount_transferred NUMERIC(19,2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_transaction_source_account
        FOREIGN KEY (source_account) REFERENCES accounts(account_number),
    CONSTRAINT  fk_tranx_destination_account
        FOREIGN KEY (destination_account) REFERENCES accounts(account_number)
);

CREATE TABLE ledger_entry(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID,
    transaction_id UUID NOT NULL,
    entry_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMP,
    settled_at TIMESTAMP,
    voided_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_ledger_account
        FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_ledger_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);




