package org.example.Entity;

public class Order_Item {
    private int lineNo;
    private int productId;
    private String sku;
    private String productName;
    private int quantity;
    private long unitPrice;
    private long lineTotal;

    public Order_Item() {
    }

    public Order_Item(int lineNo, int productId, String sku, String productName,
                      int quantity, long unitPrice, long lineTotal) {
        this.lineNo = lineNo;
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public long getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(long lineTotal) {
        this.lineTotal = lineTotal;
    }

}
