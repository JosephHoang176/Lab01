
USE [master];
GO

IF DB_ID(N'OrderReportDb') IS NULL
BEGIN
    EXEC(N'CREATE DATABASE [OrderReportDb]');
END;
GO

USE [OrderReportDb];
GO

IF OBJECT_ID(N'dbo.Users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Users
    (
        id         int            NOT NULL,
        email      nvarchar(320)  NOT NULL,
        full_name  nvarchar(200)  NOT NULL,
        role       nvarchar(20)   NOT NULL
            CONSTRAINT CK_Users_Role CHECK (role IN (N'ADMIN', N'STAFF')),

        CONSTRAINT PK_Users PRIMARY KEY (id),
        CONSTRAINT UQ_Users_Email UNIQUE (email)
    );
END;
GO

IF OBJECT_ID(N'dbo.Customers', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Customers
    (
        id               int            NOT NULL,
        code             nvarchar(50)   NOT NULL,
        name             nvarchar(200)  NOT NULL,
        tier             nvarchar(50)   NULL,
        discount_percent decimal(5, 2)  NOT NULL
            CONSTRAINT DF_Customers_DiscountPercent DEFAULT (0),
        city             nvarchar(100)  NULL,
        contact_email    nvarchar(320)  NULL,
        contact_phone    nvarchar(30)   NULL,

        CONSTRAINT PK_Customers PRIMARY KEY (id),
        CONSTRAINT UQ_Customers_Code UNIQUE (code),
        CONSTRAINT CK_Customers_DiscountPercent
            CHECK (discount_percent >= 0 AND discount_percent <= 100)
    );
END;
GO

IF OBJECT_ID(N'dbo.Products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Products
    (
        id          int           NOT NULL,
        sku         nvarchar(100) NOT NULL,
        name        nvarchar(200) NOT NULL,
        category    nvarchar(100) NULL,
        unit_price  bigint        NOT NULL,
        currency    char(3)       NOT NULL,
        stock       int           NOT NULL
            CONSTRAINT DF_Products_Stock DEFAULT (0),
        is_active   bit           NOT NULL
            CONSTRAINT DF_Products_IsActive DEFAULT (1),

        CONSTRAINT PK_Products PRIMARY KEY (id),
        CONSTRAINT UQ_Products_Sku UNIQUE (sku),
        CONSTRAINT CK_Products_UnitPrice CHECK (unit_price >= 0),
        CONSTRAINT CK_Products_Stock CHECK (stock >= 0),
        CONSTRAINT CK_Products_Currency CHECK (currency LIKE '[A-Z][A-Z][A-Z]')
    );
END;
GO

IF OBJECT_ID(N'dbo.Orders', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Orders
    (
        id               int             NOT NULL,
        code             nvarchar(50)    NOT NULL,
        customer_id      int             NOT NULL,
        customer_name    nvarchar(200)   NULL,
        created_by       int             NOT NULL,
        status           nvarchar(30)    NOT NULL
            CONSTRAINT DF_Orders_Status DEFAULT (N'DRAFT'),
        subtotal         decimal(19, 4)  NOT NULL
            CONSTRAINT DF_Orders_Subtotal DEFAULT (0),
        discount_percent decimal(5, 2)   NOT NULL
            CONSTRAINT DF_Orders_DiscountPercent DEFAULT (0),
        discount_amount  decimal(19, 4)  NOT NULL
            CONSTRAINT DF_Orders_DiscountAmount DEFAULT (0),
        tax_percent      decimal(5, 2)   NOT NULL
            CONSTRAINT DF_Orders_TaxPercent DEFAULT (0),
        tax_amount       decimal(19, 4)  NOT NULL
            CONSTRAINT DF_Orders_TaxAmount DEFAULT (0),
        total            decimal(19, 4)  NOT NULL
            CONSTRAINT DF_Orders_Total DEFAULT (0),
        currency         char(3)         NOT NULL,
        created_at       datetimeoffset(7) NOT NULL
            CONSTRAINT DF_Orders_CreatedAt DEFAULT (SYSDATETIMEOFFSET()),
        updated_at       datetimeoffset(7) NOT NULL
            CONSTRAINT DF_Orders_UpdatedAt DEFAULT (SYSDATETIMEOFFSET()),
        paid_at          datetimeoffset(7) NULL,
        fulfilled_at     datetimeoffset(7) NULL,
        cancelled_at     datetimeoffset(7) NULL,

        CONSTRAINT PK_Orders PRIMARY KEY (id),
        CONSTRAINT UQ_Orders_Code UNIQUE (code),
        CONSTRAINT FK_Orders_Customer
            FOREIGN KEY (customer_id) REFERENCES dbo.Customers(id),
        CONSTRAINT FK_Orders_CreatedBy
            FOREIGN KEY (created_by) REFERENCES dbo.Users(id),
        CONSTRAINT CK_Orders_Status
            CHECK (status IN
                (N'DRAFT', N'PENDING_PAYMENT', N'PAID',
                 N'FULFILLED', N'CANCELLED', N'UNKNOWN')),
        CONSTRAINT CK_Orders_DiscountPercent
            CHECK (discount_percent >= 0 AND discount_percent <= 100),
        CONSTRAINT CK_Orders_TaxPercent
            CHECK (tax_percent >= 0 AND tax_percent <= 100),
        CONSTRAINT CK_Orders_Amounts
            CHECK (subtotal >= 0 AND discount_amount >= 0
                   AND tax_amount >= 0 AND total >= 0),
        CONSTRAINT CK_Orders_Currency CHECK (currency LIKE '[A-Z][A-Z][A-Z]')
    );
END;
GO

IF OBJECT_ID(N'dbo.OrderItems', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.OrderItems
    (
        order_id      int           NOT NULL,
        line_no       int           NOT NULL,
        product_id    int           NOT NULL,
        sku           nvarchar(100) NOT NULL,
        product_name  nvarchar(200) NOT NULL,
        quantity      int           NOT NULL,
        unit_price    bigint        NOT NULL,
        line_total    bigint        NOT NULL,

        CONSTRAINT PK_OrderItems PRIMARY KEY (order_id, line_no),
        CONSTRAINT FK_OrderItems_Order
            FOREIGN KEY (order_id) REFERENCES dbo.Orders(id)
            ON DELETE CASCADE,
        CONSTRAINT FK_OrderItems_Product
            FOREIGN KEY (product_id) REFERENCES dbo.Products(id),
        CONSTRAINT CK_OrderItems_LineNo CHECK (line_no > 0),
        CONSTRAINT CK_OrderItems_Quantity CHECK (quantity > 0),
        CONSTRAINT CK_OrderItems_UnitPrice CHECK (unit_price >= 0),
        CONSTRAINT CK_OrderItems_LineTotal CHECK (line_total >= 0)
    );
END;
GO

IF OBJECT_ID(N'dbo.Shipments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Shipments
    (
        id                  int            NOT NULL,
        order_id            int            NOT NULL,
        order_code          nvarchar(50)   NOT NULL,
        carrier             nvarchar(100)  NULL,
        tracking_number     nvarchar(100)  NULL,
        status              nvarchar(20)   NOT NULL
            CONSTRAINT DF_Shipments_Status DEFAULT (N'PREPARING'),
        estimated_delivery  date           NULL,
        last_updated        datetimeoffset(7) NOT NULL
            CONSTRAINT DF_Shipments_LastUpdated DEFAULT (SYSDATETIMEOFFSET()),

        CONSTRAINT PK_Shipments PRIMARY KEY (id),
        CONSTRAINT UQ_Shipments_Order UNIQUE (order_id),
        CONSTRAINT FK_Shipments_Order
            FOREIGN KEY (order_id) REFERENCES dbo.Orders(id),
        CONSTRAINT CK_Shipments_Status
            CHECK (status IN
                (N'IN_TRANSIT', N'DELIVERED', N'PREPARING', N'UNKNOWN'))
    );
END;
GO

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_Orders_CustomerId_CreatedAt'
      AND object_id = OBJECT_ID(N'dbo.Orders')
)
BEGIN
    CREATE INDEX IX_Orders_CustomerId_CreatedAt
        ON dbo.Orders (customer_id, created_at);
END;
GO

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_Orders_Status_CreatedAt'
      AND object_id = OBJECT_ID(N'dbo.Orders')
)
BEGIN
    CREATE INDEX IX_Orders_Status_CreatedAt
        ON dbo.Orders (status, created_at);
END;
GO

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_OrderItems_ProductId'
      AND object_id = OBJECT_ID(N'dbo.OrderItems')
)
BEGIN
    CREATE INDEX IX_OrderItems_ProductId
        ON dbo.OrderItems (product_id);
END;
GO

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_Shipments_TrackingNumber'
      AND object_id = OBJECT_ID(N'dbo.Shipments')
)
BEGIN
    CREATE UNIQUE INDEX UX_Shipments_TrackingNumber
        ON dbo.Shipments (tracking_number)
        WHERE tracking_number IS NOT NULL;
END;
GO

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_Shipments_Status'
      AND object_id = OBJECT_ID(N'dbo.Shipments')
)
BEGIN
    CREATE INDEX IX_Shipments_Status
        ON dbo.Shipments (status);
END;
GO
