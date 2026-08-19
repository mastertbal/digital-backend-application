CREATE TABLE daily_transactions(
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    total_debit numeric(19,2) NOT NULL,
    total_credit numeric(19,2) NOT NULL,
    transaction_date date NOT NULL UNIQUE
);