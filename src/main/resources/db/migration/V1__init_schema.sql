CREATE TABLE obligations (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    category VARCHAR(50) NOT NULL,
    recurrence VARCHAR(50),
    next_payment_date DATE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    obligation_id UUID NOT NULL REFERENCES obligations(id) ON DELETE CASCADE,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
