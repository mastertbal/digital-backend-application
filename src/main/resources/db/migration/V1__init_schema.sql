CREATE TABLE accounts(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id uuid UNIQUE NOT NULL,
    account_status varchar(25) NOT NULL
             CHECK ( account_status in ('ACTIVE', 'DORMANT', 'FROZEN') ),
    account_number varchar(25) UNIQUE NOT NULL,
    balance numeric(19, 2) NOT NULL,
    account_tier varchar(25) NOT NULL
             CHECK ( account_tier in ('TIER_1', 'TIER_2', 'TIER_3') ),
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE TABLE audit_logs(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_Id uuid NOT NULL,
    user_email varchar(100) NOT NULL,
    action_type varchar(25) NOT NULL, --action type enum still empty
    entity_type varchar(25) NOT NULL
);

CREATE TABLE businesses(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    business_name varchar(100) UNIQUE NOT NULL,
    business_address varchar(100) UNIQUE NOT NULL,
    cac_number varchar(50) UNIQUE NOT NULL,
    password varchar(50) NOT NULL,
    business_email varchar(50) UNIQUE NOT NULL,
    account_number varchar(25) UNIQUE NOT NULL,
    created_at timestamp NOT NULL
);

CREATE TABLE customers(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    address varchar(100) NOT NULL,
    nin varchar(25) UNIQUE,
    bvn varchar(25) UNIQUE
);

CREATE TABLE daily_transfer_totals(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number varchar(25) NOT NULL,
    transfer_date date NOT NULL,
    total_amount numeric(19,2) NOT NULL
);

CREATE TABLE kyc_entities(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id uuid,
    account_id uuid,
    document_type varchar(25) NOT NULL
                 CHECK ( document_type in ('NIN','BVN') ),
    submitted_value varchar(25),
    status varchar(25) NOT NULL
                 CHECK ( status in ('PENDING', 'APPROVED', 'REJECTED') ),
    rejection_reason varchar(100),
    upgraded_to VARCHAR(25)
                 CHECK ( upgraded_to in ('TIER_1', 'TIER_2', 'TIER_3') ),
    submitted_at timestamp NOT NULL,
    resolved_at timestamp NOT NULL
);

CREATE TABLE login_sessions(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid,
    logged_in boolean,
    time_of_log_in timestamp,
    time_of_log_out timestamp
);

CREATE TABLE refresh_sessions(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    active_session_id varchar(50),
    user_id uuid,
    created_date timestamp,
    updated_date timestamp
);

CREATE TABLE transactions(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_type varchar(25) NOT NULL
                 CHECK ( transaction_type in ('DEPOSIT','WITHDRAWAL','TRANSFER') ),
    transaction_status varchar(25) NOT NULL
                CHECK (transaction_status in ('DECLINED','PENDING','SUCCESSFUL')),
    source_account varchar(25) REFERENCES accounts(account_number),
    destination_account varchar(25) NOT NULL REFERENCES accounts(account_number),
    amount_transferred numeric(19,2) NOT NULL,
    description varchar(50) NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE TABLE ledger_entries(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id uuid REFERENCES accounts(id),
    transaction_id uuid NOT NULL REFERENCES transactions(id),
    entry_type varchar(25) NOT NULL
               CHECK ( entry_type in ('DEBIT', 'CREDIT') ),
    status varchar(25) NOT NULL
               CHECK ( status IN ('SETTLED','PENDING','VOID') ),
    amount numeric(19,2) NOT NULL,
    created_at timestamp NOT NULL,
    settled_at timestamp,
    voided_at timestamp,
    updated_at timestamp NOT NULL
);

CREATE TABLE users(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name varchar(50) NOT NULL,
    last_name varchar(50) NOT NULL,
    email varchar(50) UNIQUE NOT NULL,
    password varchar(50) NOT NULL,
    phone_number varchar(25) UNIQUE NOT NULL,
    gender varchar(25) NOT NULL
            CHECK ( gender in ('MALE','FEMALE') ),
    date_of_birth date NOT NULL,
    role varchar(25) NOT NULL
            CHECK(role in ('ADMIN','EMPLOYEE','CUSTOMER')),
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);