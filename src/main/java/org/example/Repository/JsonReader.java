package org.example.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DTO.Order;
import org.example.interfaces.IOrderRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Repository
public class JsonReader implements IOrderRepository {

    private final ObjectMapper objectMapper;

    public JsonReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );
    }

    @Override
    public List<Order> getAllOrders() {
        InputStream inputStream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream("orders.json");

        if (inputStream == null) {
            throw new IllegalStateException(
                    "orders.json was not found"
            );
        }

        try (inputStream) {
            return objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<Order>>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read orders.json",
                    e
            );
        }
    }
}