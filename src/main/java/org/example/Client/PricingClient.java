package org.example.Client;

import org.example.DTO.PricingRequest;
import org.example.DTO.PricingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "pricing-service",
        url = "${pricing.service.url}"
)
public interface PricingClient {

    @PostMapping("/pricing/calculate")
    PricingResponse calculate(
            @RequestBody PricingRequest request
    );
}
