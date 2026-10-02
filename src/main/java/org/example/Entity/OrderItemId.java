package org.example.Entity;

import java.io.Serializable;
import java.util.Objects;

public class OrderItemId implements Serializable {

    private int orderId;
    private int lineNo;

    public OrderItemId() {
    }

    public OrderItemId(int orderId, int lineNo) {
        this.orderId = orderId;
        this.lineNo = lineNo;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof OrderItemId other)) {
            return false;
        }
        return orderId == other.orderId && lineNo == other.lineNo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, lineNo);
    }
}
