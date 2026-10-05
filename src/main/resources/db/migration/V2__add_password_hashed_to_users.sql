IF NOT EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.Users')
      AND name = N'password_hashed'
)
BEGIN
ALTER TABLE dbo.Users
    ADD password_hashed VARCHAR(100) NULL;
END
GO
