package org.example.service;

import org.example.model.DateRange;
import org.example.model.Order;
import org.example.model.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderService {
    public double calculateRevenue(List<Order> orders, String statusInput, LocalDate startDate, LocalDate endDate) {
        double totalRevenue = 0;
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            LocalDate orderDate = order.createdAt().toLocalDate();

            // 1. Kiểm tra Status: Nếu có nhập status và khác "ALL" thì mới lọc
            if (statusInput != null && !statusInput.isEmpty() && !statusInput.equalsIgnoreCase("ALL")) {
                if (!order.status().name().equalsIgnoreCase(statusInput)) {
                    continue; // Không khớp status thì bỏ qua
                }
            }

            // 2. Kiểm tra ngày bắt đầu: Nếu có nhập startDate thì mới lọc
            if (startDate != null) {
                if (orderDate.isBefore(startDate)) {
                    continue; // Trước ngày bắt đầu thì bỏ qua
                }
            }

            // 3. Kiểm tra ngày kết thúc: Nếu có nhập endDate thì mới lọc
            if (endDate != null) {
                if (orderDate.isAfter(endDate)) {
                    continue; // Sau ngày kết thúc thì bỏ qua
                }
            }

            // Nếu vượt qua tất cả các bộ lọc ở trên thì cộng dồn tiền
            totalRevenue += order.total();
        }

        return totalRevenue;
    }
}
