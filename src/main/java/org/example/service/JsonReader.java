package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.model.Order;

import java.io.InputStream;
import java.util.List;

public class JsonReader {
    private final ObjectMapper objectMapper;

    public JsonReader() {
        this.objectMapper = new ObjectMapper();
        // Đăng ký JavaTimeModule để xử lý kiểu thời gian OffsetDateTime/LocalDate
        this.objectMapper.registerModule(new JavaTimeModule());
        // Tránh lỗi nếu JSON có thêm trường lạ chưa khai báo trong Record
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<Order> loadOrders(String resourceFileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceFileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Không tìm thấy file JSON trong resources: " + resourceFileName);
            }
            return objectMapper.readValue(inputStream, new TypeReference<List<Order>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file JSON: " + e.getMessage(), e);
        }
    }
}
