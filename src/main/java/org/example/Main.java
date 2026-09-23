package org.example;

import org.example.model.DateRange;
import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.service.JsonReader;
import org.example.service.OrderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        JsonReader reader = new JsonReader();
        List<Order> allOrders = reader.loadOrders("orders.json");

        OrderService service = new OrderService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("==========================================");
        System.out.println("   HE THONG LOC VA TRA CUU THONG TIN DOANH THU  ");
        System.out.println("==========================================");

        while (true) {
            System.out.println("\n------------------------------------------");
            System.out.println("( PRESS [Enter] BO QUA DE LAY TAT CA, HOAC gO EXIT DE THOAT)");

            System.out.print("1. NHAP STATUS (CANCELLED, PAID, FULFILLED...): ");
            String status = scanner.nextLine().trim();

            if (status.equalsIgnoreCase("EXIT")) {
                System.out.println("CAM ON BAN DA SU DUNG!");
                break;
            }

            System.out.print("2. NHAP NGAY BAT DAU (yyyy-MM-dd): ");
            String startDateStr = scanner.nextLine().trim();

            System.out.print("3. NHAP NGAY KET THUC (yyyy-MM-dd): ");
            String endDateStr = scanner.nextLine().trim();

            try {
                // Nếu người dùng không nhập gì, chuyển thành null
                LocalDate startDate = startDateStr.isEmpty() ? null : LocalDate.parse(startDateStr);
                LocalDate endDate = endDateStr.isEmpty() ? null : LocalDate.parse(endDateStr);

                // Tính toán doanh thu
                double totalRevenue = service.calculateRevenue(allOrders, status, startDate, endDate);

                // In kết quả hiển thị
                String displayStatus = status.isEmpty() ? "TAT CA" : status.toUpperCase();
                String displayFrom = (startDate == null) ? "" : startDate.toString();
                String displayTo = (endDate == null) ? "" : endDate.toString();

                System.out.println("\n---------------- KET QUA ----------------");
                System.out.println("TRANG THAI LOC : " + displayStatus);
                System.out.println("FROM        : " + displayFrom);
                System.out.println("TO       : " + displayTo);
                System.out.printf("REVENUE : %,.0f VND%n", totalRevenue);
                System.out.println("-----------------------------------------");

            } catch (Exception e) {
                System.out.println("\n[ERR]: SAI DINH DANG THOI GIAN (yyyy-MM-dd, VD: 2026-07-09). VUI LONG THU LAI!");
            }
        }

        scanner.close();
    }
}




