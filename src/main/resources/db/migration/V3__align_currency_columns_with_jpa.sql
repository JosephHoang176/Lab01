ALTER TABLE dbo.products
    ALTER COLUMN currency TYPE VARCHAR(3);

ALTER TABLE dbo.orders
    ALTER COLUMN currency TYPE VARCHAR(3);
