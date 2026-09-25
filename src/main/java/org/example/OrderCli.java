package org.example;

import org.example.DTO.DateRange;
import org.example.DTO.Order;
import org.example.enums.OrderStatus;
import org.example.Service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

@Component
public class OrderCli implements CommandLineRunner {

    private final OrderService orderService;

    public OrderCli(OrderService orderService) {

        this.orderService = orderService;
    }

    @Override
    public void run(String... args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("======================================");
        System.out.println("          ORDER REPORT CLI");
        System.out.println("======================================");

        OrderStatus status = readStatus(scanner);

        LocalDate fromDate = readDate(
                scanner,
                "From date (yyyy-MM-dd, blank = all): "
        );

        LocalDate toDate = readDate(
                scanner,
                "To date (yyyy-MM-dd, blank = all): "
        );

        DateRange dateRange =
                createDateRange(fromDate, toDate);


        List<Order> orders =
                orderService.findFilteredOrders(
                        status,
                        dateRange
                );

        long start = System.currentTimeMillis();

        orderService.getShipments(orders);

        long end = System.currentTimeMillis();

        System.out.println(
                "Shipping enrichment time: "
                        + (end - start)
                        + " ms"
        );

        double revenue = orderService.getRevenue(status, dateRange);

        System.out.println();
        System.out.println("======================================");
        System.out.println("               RESULT");
        System.out.println("======================================");

        System.out.println(
                "Status: "
                        + (status == null ? "ALL" : status)
        );

        System.out.println(
                "From: "
                        + (fromDate == null ? "ALL" : fromDate)
        );

        System.out.println(
                "To: "
                        + (toDate == null ? "ALL" : toDate)
        );

        System.out.println(
                "Order count: " + orders.size()
        );

        System.out.printf(
                "Revenue: %.0f%n",
                revenue
        );

        System.out.println();
        System.out.println("Orders:");

        for (Order order : orders) {

            System.out.printf(
                    "%s | %s | %s | %.0f %s%n",
                    order.code(),
                    order.status(),
                    order.createdAt() == null
                            ? "N/A"
                            : order.createdAt().toLocalDate(),
                    order.total(),
                    order.currency()
            );
        }

        System.out.println("======================================");
    }

    private OrderStatus readStatus(Scanner scanner) {

        while (true) {

            System.out.print(
                    "Status (PAID, FULFILLED, CANCELLED, ... / blank = all): "
            );

            String input =
                    scanner.nextLine().trim();

            if (input.isBlank()) {
                return null;
            }

            OrderStatus status =
                    OrderStatus.fromString(input);

            if (status != OrderStatus.UNKNOWN
                    || input.equalsIgnoreCase("UNKNOWN")) {

                return status;
            }

            System.out.println(
                    "Invalid status. Please try again."
            );
        }
    }

    private LocalDate readDate(
            Scanner scanner,
            String message
    ) {

        while (true) {

            System.out.print(message);

            String input =
                    scanner.nextLine().trim();

            if (input.isBlank()) {
                return null;
            }

            try {

                return LocalDate.parse(input);

            } catch (DateTimeParseException e) {

                System.out.println(
                        "Invalid date. Example: 2026-07-01"
                );
            }
        }
    }

    private DateRange createDateRange(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        if (fromDate == null && toDate == null) {
            return null;
        }

        return new DateRange(
                fromDate,
                toDate
        );
    }
}