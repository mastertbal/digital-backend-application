CREATE TABLE users (
    id UUID PRIMARY KEY,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    email VARCHAR(255) NOT NULL UNIQUE,

    password VARCHAR(255) NOT NULL,

    phone_number VARCHAR(20) NOT NULL UNIQUE,

    gender VARCHAR(20) NOT NULL,

    date_of_birth DATE NOT NULL,

    role VARCHAR(50) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customers (
    id UUID PRIMARY KEY,

    address VARCHAR(255),

    nin VARCHAR(50) UNIQUE,

    bvn VARCHAR(50) UNIQUE,

    CONSTRAINT fk_customer_user
        FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE account (
    id UUID PRIMARY KEY,

    owner_id UUID UNIQUE NOT NULL,

    account_status VARCHAR(50) NOT NULL,

    account_number VARCHAR(255) UNIQUE NOT NULL,

    balance DECIMAL(19,2) DEFAULT 0.00,

    account_tier VARCHAR(50) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_owner_id
        FOREIGN KEY (owner_id) REFERENCES customers(id) ON DELETE CASCADE
);