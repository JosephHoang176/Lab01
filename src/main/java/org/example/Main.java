package org.example;

import org.example.model.DateRange;
import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.service.JsonReader;
import org.example.service.OrderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        JsonReader jsonReader = new JsonReader();
        OrderService orderService = new OrderService();

        // 1. Đọc file JSON
        List<Order> allOrders = jsonReader.loadOrders("orders.json");

        // 2. Thiết lập bộ lọc (Ví dụ: Tháng 07/2026)
        DateRange dateRange = new DateRange(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );

        List<Order> filteredOrders = orderService.filterOrders(allOrders, null, dateRange);

        // --- BÁO CÁO (PRINT REPORT) ---
        System.out.println("==================================================");
        System.out.println("            ORDER SUMMARY REPORT                  ");
        System.out.println("==================================================");
        System.out.println("Total orders loaded: " + allOrders.size());
        System.out.println("Filtered orders count: " + filteredOrders.size());
        System.out.printf("Total Revenue: %,.0f VND\n", orderService.calculateTotalRevenue(filteredOrders));
        System.out.println("--------------------------------------------------");

        // 3. Doanh thu theo Trạng thái (Compute revenue per status)
        System.out.println("\n[ REVENUE PER STATUS ]");
        Map<OrderStatus, Double> revenuePerStatus = orderService.computeRevenuePerStatus(filteredOrders);
        revenuePerStatus.forEach((status, revenue) ->
                System.out.printf(" - %-15s : %,15.0f VND\n", status, revenue)
        );

        // 4. Doanh thu theo Ngày (Compute revenue per day)
        System.out.println("\n[ REVENUE PER DAY ]");
        Map<LocalDate, Double> revenuePerDay = orderService.computeRevenuePerDay(filteredOrders);
        revenuePerDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Sắp xếp theo thứ tự ngày tăng dần
                .forEach(entry ->
                        System.out.printf(" - %-15s : %,15.0f VND\n", entry.getKey(), entry.getValue())
                );

        System.out.println("==================================================");
    }
}

