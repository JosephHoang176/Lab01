package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSetIntegrityTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void referenceDataHasExpectedCountsAndUniqueKeys() throws Exception {
        JsonNode users = read("users.json");
        JsonNode products = read("products.json");
        JsonNode customers = read("customers.json");

        assertEquals(5, users.size());
        assertEquals(50, products.size());
        assertEquals(20, customers.size());
        assertEquals(3, countInactive(products));
        assertUnique(users, "email");
        assertUnique(products, "sku");
        assertUnique(customers, "code");
    }

    @Test
    void orderDataHasExpectedRelationships() throws Exception {
        JsonNode orders = read("orders.json");
        JsonNode shipments = read("shipments.json");

        assertEquals(200, orders.size());
        assertEquals(137, shipments.size());
        assertTrue(orders.findValues("lines").stream()
                .mapToInt(JsonNode::size)
                .sum() > 0);
    }

    private JsonNode read(String resource) throws Exception {
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Missing resource: " + resource);
            }
            return objectMapper.readTree(stream);
        }
    }

    private int countInactive(JsonNode products) {
        int count = 0;
        for (JsonNode product : products) {
            if (!product.get("isActive").asBoolean()) {
                count++;
            }
        }
        return count;
    }

    private void assertUnique(JsonNode rows, String field) {
        Set<String> values = new HashSet<>();
        for (JsonNode row : rows) {
            assertTrue(values.add(row.get(field).asText()),
                    "Duplicate " + field + ": " + row.get(field).asText());
        }
    }
}
