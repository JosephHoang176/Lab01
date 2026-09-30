package org.example.Controller;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemControllerTest {

    private final SystemController controller =
            new SystemController("order-report-cli", "1.0-SNAPSHOT");

    @Test
    void healthReturnsUp() {
        assertEquals(Map.of("status", "UP"), controller.health());
    }

    @Test
    void versionReturnsApplicationMetadata() {
        assertEquals(
                Map.of(
                        "name", "order-report-cli",
                        "version", "1.0-SNAPSHOT"
                ),
                controller.version()
        );
    }
}
