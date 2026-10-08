package org.example.integration;

import org.example.Entity.Order;
import org.example.Controller.OrderController;
import org.example.Service.OrderService;
import org.example.Security.JwtService;
import org.example.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtService jwtService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(10);
        order.setCode("ORD-10");
        order.setStatus(OrderStatus.DRAFT);
        order.setLines(List.of());
    }

    @Test
    void listsOrders() throws Exception {
        when(orderService.findFilteredOrders(null, null)).thenReturn(List.of(order));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("DRAFT"));
    }

    @Test
    void listsOrdersWithStatusAndDateFilters() throws Exception {
        when(orderService.findFilteredOrders(OrderStatus.PAID, null)).thenReturn(List.of(order));

        mockMvc.perform(get("/orders").param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("ORD-10"));

        verify(orderService).findFilteredOrders(OrderStatus.PAID, null);
    }

    @Test
    void getsOrderById() throws Exception {
        when(orderService.findOrderById(10)).thenReturn(order);

        mockMvc.perform(get("/orders/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.code").value("ORD-10"));
    }

    @Test
    void createsOrder() throws Exception {
        when(orderService.createOrder(any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/orders")
                        .contentType("application/json")
                        .content(orderJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(orderService).createOrder(any(Order.class));
    }

    @Test
    void changesOrderStatus() throws Exception {
        order.setStatus(OrderStatus.PAID);
        when(orderService.changeStatus(10, OrderStatus.PAID)).thenReturn(order);

        mockMvc.perform(patch("/orders/status")
                        .param("orderId", "10")
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void cancelsOrder() throws Exception {
        order.setStatus(OrderStatus.CANCELLED);
        when(orderService.cancelOrder(10)).thenReturn(order);

        mockMvc.perform(patch("/orders/10/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(orderService).cancelOrder(10);
    }

    private String orderJson() {
        return """
                {
                  "id": 10,
                  "code": "ORD-10",
                  "customerId": 1,
                  "customerName": "Customer",
                  "createdBy": 1,
                  "status": "DRAFT",
                  "lines": [],
                  "subtotal": 0,
                  "discountPercent": 0,
                  "discountAmount": 0,
                  "taxPercent": 0,
                  "taxAmount": 0,
                  "total": 0,
                  "currency": "USD"
                }
                """;
    }
}
