package org.example.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DTO.OrderDTO;
import org.example.Entity.Order;
import org.example.interfaces.IOrderRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Repository
public class JsonReader implements IOrderRepository {

    private final ObjectMapper objectMapper;
    private final Path filePath = Path.of("data", "orders.json");

    public JsonReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );
    }

    @Override
    public List<Order> getAllOrders() {
        if (!Files.exists(filePath)) {
            throw new IllegalStateException(
                    "orders.json was not found: " + filePath
            );
        }

        try {
            return objectMapper.readValue(
                    filePath.toFile(),
                    new TypeReference<List<Order>>() {
                    }
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read orders.json",
                    e
            );
        }
    }

    @Override
    public Order getOrderById(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Id khong hop le");
        }
        List<Order> orders = getAllOrders();
        for (int i = 0; i < orders.size(); i++) {
            if (orderId == orders.get(i).getId()) {
                return orders.get(i);
            }
        }
        return null;
    }

    @Override
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "Order khong hop le"
            );
        }
        List<Order> orders = getAllOrders();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId() == order.getId()) {
                throw new IllegalArgumentException(
                        "Order nay da ton tai"
                );
            }
        }
        orders.add(order);
        writeOrders(orders);
        return order;
    }

    @Override
    public Order update(Order order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "Order khong hop le"
            );
        }
        List<Order> orders = getAllOrders();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId() == order.getId()) {
                orders.set(i, order);
                writeOrders(orders);
                return order;
            }
        }
        throw new IllegalArgumentException(
                "Order khong ton tai"
        );
    }

    //Ghi vào orders.json.
    private void writeOrders(List<Order> orders) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), orders);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to write orders.json",
                    e
            );
        }
    }
}