IF OBJECT_ID(N'dbo.idempotency_keys', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.idempotency_keys
    (
        idempotency_key NVARCHAR(255) NOT NULL,
        request_method  NVARCHAR(10)  NOT NULL,
        request_path    NVARCHAR(500) NOT NULL,
        owner_key       NVARCHAR(320) NOT NULL,
        status          NVARCHAR(20)  NOT NULL,
        response_body   NVARCHAR(MAX) NULL,
        expires_at      DATETIME2(7)  NOT NULL,
        created_at      DATETIME2(7)  NOT NULL
            CONSTRAINT DF_idempotency_keys_created_at DEFAULT SYSUTCDATETIME(),
        completed_at    DATETIME2(7) NULL,

        CONSTRAINT PK_idempotency_keys
            PRIMARY KEY (idempotency_key, request_method, request_path, owner_key),
        CONSTRAINT CK_idempotency_keys_status
            CHECK (status IN (N'PROCESSING', N'COMPLETED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_idempotency_keys_expires_at'
      AND object_id = OBJECT_ID(N'dbo.idempotency_keys')
)
BEGIN
    CREATE INDEX IX_idempotency_keys_expires_at
        ON dbo.idempotency_keys (expires_at);
END
GO