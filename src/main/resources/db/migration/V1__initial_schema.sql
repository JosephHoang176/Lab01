IF OBJECT_ID(N'dbo.Users', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Users
(
    id         INT            NOT NULL,
    email      NVARCHAR(320)  NOT NULL,
    full_name  NVARCHAR(200)  NOT NULL,
    role       NVARCHAR(20)   NOT NULL
            CONSTRAINT CK_Users_Role CHECK (role IN (N'ADMIN', N'STAFF')),

    CONSTRAINT PK_Users PRIMARY KEY (id),
    CONSTRAINT UQ_Users_Email UNIQUE (email)
);
END
GO
