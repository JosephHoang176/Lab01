CREATE SCHEMA IF NOT EXISTS dbo;

CREATE TABLE dbo.users
(
    id        INTEGER      NOT NULL,
    email     VARCHAR(320) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    role      VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'STAFF'))
);

CREATE TABLE dbo.customers
(
    id                INTEGER      NOT NULL,
    code              VARCHAR(50)   NOT NULL,
    name              VARCHAR(200)  NOT NULL,
    tier              VARCHAR(50),
    discount_percent  NUMERIC(5, 2) NOT NULL DEFAULT 0,
    city              VARCHAR(100),
    contact_email     VARCHAR(320),
    contact_phone     VARCHAR(30),
    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uq_customers_code UNIQUE (code),
    CONSTRAINT ck_customers_discount_percent
        CHECK (discount_percent BETWEEN 0 AND 100)
);

CREATE TABLE dbo.products
(
    id         INTEGER      NOT NULL,
    sku        VARCHAR(100) NOT NULL,
    name       VARCHAR(200) NOT NULL,
    category   VARCHAR(100),
    unit_price BIGINT       NOT NULL,
    currency   CHAR(3)      NOT NULL,
    stock      INTEGER      NOT NULL DEFAULT 0,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uq_products_sku UNIQUE (sku),
    CONSTRAINT ck_products_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_products_stock CHECK (stock >= 0),
    CONSTRAINT ck_products_currency CHECK (currency ~ '^[A-Z]{3}$')
);

CREATE TABLE dbo.orders
(
    id                INTEGER        NOT NULL,
    code              VARCHAR(50)     NOT NULL,
    customer_id       INTEGER        NOT NULL,
    customer_name     VARCHAR(200),
    created_by        INTEGER        NOT NULL,
    status            VARCHAR(30)     NOT NULL DEFAULT 'DRAFT',
    subtotal          NUMERIC(19, 4)  NOT NULL DEFAULT 0,
    discount_percent  NUMERIC(5, 2)   NOT NULL DEFAULT 0,
    discount_amount   NUMERIC(19, 4)  NOT NULL DEFAULT 0,
    tax_percent       NUMERIC(5, 2)   NOT NULL DEFAULT 0,
    tax_amount        NUMERIC(19, 4)  NOT NULL DEFAULT 0,
    total             NUMERIC(19, 4)  NOT NULL DEFAULT 0,
    currency          CHAR(3)         NOT NULL,
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at           TIMESTAMPTZ,
    fulfilled_at      TIMESTAMPTZ,
    cancelled_at      TIMESTAMPTZ,
    pricing_status    VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT uq_orders_code UNIQUE (code),
    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id) REFERENCES dbo.customers (id),
    CONSTRAINT fk_orders_created_by
        FOREIGN KEY (created_by) REFERENCES dbo.users (id),
    CONSTRAINT ck_orders_status CHECK
        (status IN ('DRAFT', 'PENDING_PAYMENT', 'PAID',
                    'FULFILLED', 'CANCELLED', 'UNKNOWN')),
    CONSTRAINT ck_orders_discount_percent
        CHECK (discount_percent BETWEEN 0 AND 100),
    CONSTRAINT ck_orders_tax_percent
        CHECK (tax_percent BETWEEN 0 AND 100),
    CONSTRAINT ck_orders_amounts
        CHECK (subtotal >= 0 AND discount_amount >= 0
               AND tax_amount >= 0 AND total >= 0),
    CONSTRAINT ck_orders_currency CHECK (currency ~ '^[A-Z]{3}$')
);

CREATE TABLE dbo.order_items
(
    order_id     INTEGER      NOT NULL,
    line_no      INTEGER      NOT NULL,
    product_id   INTEGER      NOT NULL,
    sku          VARCHAR(100) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity     INTEGER      NOT NULL,
    unit_price   BIGINT       NOT NULL,
    line_total   BIGINT       NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (order_id, line_no),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES dbo.orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES dbo.products (id),
    CONSTRAINT ck_order_items_line_no CHECK (line_no > 0),
    CONSTRAINT ck_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_order_items_line_total CHECK (line_total >= 0)
);

CREATE TABLE dbo.shipments
(
    id                 INTEGER      NOT NULL,
    order_id           INTEGER      NOT NULL,
    order_code         VARCHAR(50)  NOT NULL,
    carrier            VARCHAR(100),
    tracking_number    VARCHAR(100),
    status             VARCHAR(20)  NOT NULL DEFAULT 'PREPARING',
    estimated_delivery DATE,
    last_updated       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_shipments PRIMARY KEY (id),
    CONSTRAINT uq_shipments_order UNIQUE (order_id),
    CONSTRAINT fk_shipments_order
        FOREIGN KEY (order_id) REFERENCES dbo.orders (id),
    CONSTRAINT ck_shipments_status
        CHECK (status IN ('IN_TRANSIT', 'DELIVERED', 'PREPARING', 'UNKNOWN'))
);

CREATE INDEX ix_orders_customer_created_at
    ON dbo.orders (customer_id, created_at);
CREATE INDEX ix_orders_status_created_at
    ON dbo.orders (status, created_at);
CREATE INDEX ix_order_items_product_id
    ON dbo.order_items (product_id);
CREATE UNIQUE INDEX ux_shipments_tracking_number
    ON dbo.shipments (tracking_number)
    WHERE tracking_number IS NOT NULL;
CREATE INDEX ix_shipments_status
    ON dbo.shipments (status);
