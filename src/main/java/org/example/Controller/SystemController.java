package org.example.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SystemController {

    private final String applicationName;
    private final String applicationVersion;

    public SystemController(
            @Value("${info.app.name:order-report-cli}") String applicationName,
            @Value("${info.app.version:unknown}") String applicationVersion
    ) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/version")
    public Map<String, String> version() {
        return Map.of(
                "name", applicationName,
                "version", applicationVersion
        );
    }
}
