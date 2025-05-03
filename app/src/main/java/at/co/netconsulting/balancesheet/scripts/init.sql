\c incomeexpense;

-- Keep existing table
CREATE TABLE IF NOT EXISTS incomeexpense (
    id SERIAL PRIMARY KEY,
    orderdate DATE NOT NULL,
    who VARCHAR(255),
    position VARCHAR(255),
    income NUMERIC(16,2),
    expense NUMERIC(16,2),
    location VARCHAR,
    comment VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add new transaction_log table
CREATE TABLE IF NOT EXISTS transaction_log (
    transaction_id VARCHAR(50) PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN DEFAULT FALSE
);