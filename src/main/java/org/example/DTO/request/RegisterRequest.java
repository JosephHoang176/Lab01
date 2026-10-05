package org.example.DTO.request;

public record RegisterRequest(
    String email,
    String fullName,
    String password
) {
}
