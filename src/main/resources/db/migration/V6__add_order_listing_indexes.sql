-- Indexes used by the filtered order listing and order-item lookups.
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_Orders_Status_CreatedAt'
      AND object_id = OBJECT_ID(N'dbo.Orders')
)
BEGIN
    CREATE INDEX IX_Orders_Status_CreatedAt
        ON dbo.Orders(status, created_at)
        INCLUDE (customer_id, total, currency);
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_OrderItems_ProductId'
      AND object_id = OBJECT_ID(N'dbo.OrderItems')
)
BEGIN
    CREATE INDEX IX_OrderItems_ProductId
        ON dbo.OrderItems(product_id);
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_Products_Category_Active'
      AND object_id = OBJECT_ID(N'dbo.Products')
)
BEGIN
    CREATE INDEX IX_Products_Category_Active
        ON dbo.Products(category, is_active)
        INCLUDE (name, unit_price, stock, currency);
END
GO
