package org.example.DTO.response;

public record ApiResponse <T>(
    String message,
    String code,
    T data
) {}
