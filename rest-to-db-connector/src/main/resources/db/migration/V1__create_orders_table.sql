CREATE TABLE orders (
    id            BIGSERIAL PRIMARY KEY,
    order_id      VARCHAR(64)  NOT NULL,
    customer_id   VARCHAR(64)  NOT NULL,
    product_name  VARCHAR(255) NOT NULL,
    quantity      INTEGER      NOT NULL,
    price         NUMERIC(12, 2) NOT NULL,
    total_amount  NUMERIC(12, 2) NOT NULL,
    order_date    DATE         NOT NULL,
    status        VARCHAR(32)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_orders_order_id UNIQUE (order_id)
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_orders_status ON orders (status);
