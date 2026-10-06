IF OBJECT_ID(N'dbo.Products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Products
    (
        id         INT NOT NULL CONSTRAINT PK_Products PRIMARY KEY,
        sku        NVARCHAR(100) NOT NULL,
        name       NVARCHAR(200) NOT NULL,
        category   NVARCHAR(100) NULL,
        unit_price BIGINT NOT NULL,
        currency   CHAR(3) NOT NULL,
        stock      INT NOT NULL,
        is_active  BIT NOT NULL,
        CONSTRAINT UQ_Products_Sku UNIQUE (sku),
        CONSTRAINT CK_Products_Stock CHECK (stock >= 0)
    );
END
GO

IF OBJECT_ID(N'dbo.Orders', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Orders
    (
        id                INT NOT NULL CONSTRAINT PK_Orders PRIMARY KEY,
        code              NVARCHAR(100) NULL,
        customer_id       INT NOT NULL,
        customer_name     NVARCHAR(200) NULL,
        created_by        INT NOT NULL,
        status            NVARCHAR(30) NOT NULL,
        subtotal          DECIMAL(19, 4) NOT NULL,
        discount_percent  DECIMAL(19, 4) NOT NULL,
        discount_amount   DECIMAL(19, 4) NOT NULL,
        tax_percent       DECIMAL(19, 4) NOT NULL,
        tax_amount        DECIMAL(19, 4) NOT NULL,
        total             DECIMAL(19, 4) NOT NULL,
        currency          CHAR(3) NULL,
        created_at        DATETIMEOFFSET NULL,
        updated_at        DATETIMEOFFSET NULL,
        paid_at           DATETIMEOFFSET NULL,
        fulfilled_at      DATETIMEOFFSET NULL,
        cancelled_at      DATETIMEOFFSET NULL
    );
END
GO

IF OBJECT_ID(N'dbo.OrderItems', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.OrderItems
    (
        order_id     INT NOT NULL,
        line_no      INT NOT NULL,
        product_id   INT NOT NULL,
        sku          NVARCHAR(100) NOT NULL,
        product_name NVARCHAR(200) NOT NULL,
        quantity     INT NOT NULL,
        unit_price   BIGINT NOT NULL,
        line_total   BIGINT NOT NULL,
        CONSTRAINT PK_OrderItems PRIMARY KEY (order_id, line_no),
        CONSTRAINT FK_OrderItems_Orders FOREIGN KEY (order_id) REFERENCES dbo.Orders(id),
        CONSTRAINT CK_OrderItems_Quantity CHECK (quantity > 0)
    );
END
GO
