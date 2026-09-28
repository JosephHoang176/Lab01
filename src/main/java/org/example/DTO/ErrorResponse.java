package org.example.DTO;

public record ErrorResponse (
        int status,
        String message,
        String path,
        String correlationId
) {}
