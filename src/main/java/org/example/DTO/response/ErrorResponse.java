package org.example.DTO.response;

public record ErrorResponse (
        int status,
        String message,
        String path,
        String correlationId
) {}
