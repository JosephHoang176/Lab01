package org.example.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DTO.Shipment;
import org.example.Exception.ShippingTimeoutException;
import org.example.interfaces.ShippingClient;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

@Component
public class MockShippingClient implements ShippingClient {

    private final ObjectMapper objectMapper;

    public MockShippingClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Shipment findShipmentStatusByOrderId(int orderId) {

        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Order id khong hop le. Vui long nhap lai."
            );
        }

        String url =
                "http://[::1]:3001/shipments/" + orderId;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(1))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient().send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() == 404) {
                return null;
            }

            return objectMapper.readValue(
                    response.body(),
                    Shipment.class
            );

        } catch (HttpTimeoutException e) {
            throw new ShippingTimeoutException(e);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}