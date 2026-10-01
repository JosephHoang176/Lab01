package db.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;

public class V4__seed_orderhub_reference_and_order_data extends BaseJavaMigration {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        insertCustomers(connection, readArray("customers.json"));
        insertOrders(connection, readArray("orders.json"));
        insertOrderItems(connection, readArray("orders.json"));
        insertShipments(connection, readArray("shipments.json"));
    }

    private void insertCustomers(Connection connection, JsonNode customers)
            throws SQLException {
        String sql = """
                INSERT INTO dbo.customers
                    (id, code, name, tier, discount_percent, city,
                     contact_email, contact_phone)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (JsonNode customer : customers) {
                statement.setInt(1, customer.get("id").asInt());
                statement.setString(2, customer.get("code").asText());
                statement.setString(3, customer.get("name").asText());
                setNullableString(statement, 4, customer, "tier");
                statement.setBigDecimal(5, decimal(customer, "discountPercent"));
                setNullableString(statement, 6, customer, "city");
                setNullableString(statement, 7, customer, "contactEmail");
                setNullableString(statement, 8, customer, "contactPhone");
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertOrders(Connection connection, JsonNode orders)
            throws SQLException {
        String sql = """
                INSERT INTO dbo.orders
                    (id, code, customer_id, customer_name, created_by, status,
                     subtotal, discount_percent, discount_amount, tax_percent,
                     tax_amount, total, currency, created_at, updated_at,
                     paid_at, fulfilled_at, cancelled_at, pricing_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (JsonNode order : orders) {
                statement.setInt(1, order.get("id").asInt());
                statement.setString(2, order.get("code").asText());
                statement.setInt(3, order.get("customerId").asInt());
                setNullableString(statement, 4, order, "customerName");
                statement.setInt(5, order.get("createdBy").asInt());
                statement.setString(6, order.get("status").asText());
                statement.setBigDecimal(7, decimal(order, "subtotal"));
                statement.setBigDecimal(8, decimal(order, "discountPercent"));
                statement.setBigDecimal(9, decimal(order, "discountAmount"));
                statement.setBigDecimal(10, decimal(order, "taxPercent"));
                statement.setBigDecimal(11, decimal(order, "taxAmount"));
                statement.setBigDecimal(12, decimal(order, "total"));
                statement.setString(13, order.get("currency").asText());
                setTimestamp(statement, 14, order, "createdAt", false);
                setTimestamp(statement, 15, order, "updatedAt", false);
                setTimestamp(statement, 16, order, "paidAt", true);
                setTimestamp(statement, 17, order, "fulfilledAt", true);
                setTimestamp(statement, 18, order, "cancelledAt", true);
                statement.setString(19, "COMPLETED");
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertOrderItems(Connection connection, JsonNode orders)
            throws SQLException {
        String sql = """
                INSERT INTO dbo.order_items
                    (order_id, line_no, product_id, sku, product_name,
                     quantity, unit_price, line_total)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (JsonNode order : orders) {
                for (JsonNode line : order.get("lines")) {
                    statement.setInt(1, order.get("id").asInt());
                    statement.setInt(2, line.get("lineNo").asInt());
                    statement.setInt(3, line.get("productId").asInt());
                    statement.setString(4, line.get("sku").asText());
                    statement.setString(5, line.get("productName").asText());
                    statement.setInt(6, line.get("quantity").asInt());
                    statement.setLong(7, line.get("unitPrice").asLong());
                    statement.setLong(8, line.get("lineTotal").asLong());
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
    }

    private void insertShipments(Connection connection, JsonNode shipments)
            throws SQLException {
        String sql = """
                INSERT INTO dbo.shipments
                    (id, order_id, order_code, carrier, tracking_number,
                     status, estimated_delivery, last_updated)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (JsonNode shipment : shipments) {
                statement.setInt(1, shipment.get("id").asInt());
                statement.setInt(2, shipment.get("orderId").asInt());
                statement.setString(3, shipment.get("orderCode").asText());
                setNullableString(statement, 4, shipment, "carrier");
                setNullableString(statement, 5, shipment, "trackingNumber");
                statement.setString(6, shipment.get("status").asText());
                if (shipment.hasNonNull("estimatedDelivery")) {
                    statement.setDate(7, Date.valueOf(
                            shipment.get("estimatedDelivery").asText()));
                } else {
                    statement.setNull(7, java.sql.Types.DATE);
                }
                setTimestamp(statement, 8, shipment, "lastUpdated", false);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private JsonNode readArray(String resource) throws IOException {
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException(
                        "Migration resource was not found: " + resource);
            }
            return objectMapper.readTree(stream);
        }
    }

    private BigDecimal decimal(JsonNode node, String field) {
        return node.get(field).decimalValue();
    }

    private void setNullableString(
            PreparedStatement statement,
            int index,
            JsonNode node,
            String field
    ) throws SQLException {
        if (node.hasNonNull(field)) {
            statement.setString(index, node.get(field).asText());
        } else {
            statement.setNull(index, java.sql.Types.VARCHAR);
        }
    }

    private void setTimestamp(
            PreparedStatement statement,
            int index,
            JsonNode node,
            String field,
            boolean nullable
    ) throws SQLException {
        if (node.hasNonNull(field)) {
            statement.setTimestamp(
                    index,
                    Timestamp.from(OffsetDateTime.parse(
                            node.get(field).asText()).toInstant()));
        } else if (nullable) {
            statement.setNull(index, java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
        } else {
            throw new IllegalArgumentException(
                    "Required timestamp is missing: " + field);
        }
    }
}
