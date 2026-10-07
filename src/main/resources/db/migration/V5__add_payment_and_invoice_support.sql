IF COL_LENGTH(N'dbo.Orders', N'payment_reference') IS NULL
BEGIN
    ALTER TABLE dbo.Orders ADD payment_reference NVARCHAR(200) NULL;
END
GO

IF OBJECT_ID(N'dbo.PaymentWebhookEvents', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PaymentWebhookEvents
    (
        id          BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PaymentWebhookEvents PRIMARY KEY,
        event_id    NVARCHAR(200) NOT NULL,
        order_id    INT NOT NULL,
        received_at DATETIMEOFFSET NOT NULL,
        CONSTRAINT UQ_PaymentWebhookEvents_EventId UNIQUE (event_id),
        CONSTRAINT FK_PaymentWebhookEvents_Orders FOREIGN KEY (order_id) REFERENCES dbo.Orders(id)
    );
END
GO

IF OBJECT_ID(N'dbo.Invoices', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Invoices
    (
        id                BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Invoices PRIMARY KEY,
        order_id          INT NOT NULL,
        original_filename NVARCHAR(255) NOT NULL,
        stored_filename   NVARCHAR(255) NOT NULL,
        content_type      NVARCHAR(100) NOT NULL,
        size_bytes        BIGINT NOT NULL,
        sha256            CHAR(64) NOT NULL,
        storage_path      NVARCHAR(1000) NOT NULL,
        uploaded_at       DATETIMEOFFSET NOT NULL,
        CONSTRAINT FK_Invoices_Orders FOREIGN KEY (order_id) REFERENCES dbo.Orders(id)
    );
END
GO
