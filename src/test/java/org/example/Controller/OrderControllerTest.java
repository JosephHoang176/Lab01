package org.example.Controller;

import org.example.Service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void listWithoutStatusReturnsAllOrders() throws Exception {
        when(orderService.findFilteredOrders(isNull(), isNull()))
                .thenReturn(List.of());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .build();

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());

        verify(orderService).findFilteredOrders(isNull(), isNull());
    }
}
