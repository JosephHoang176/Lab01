package org.example.DTO.response;

public record TokenResponse(
      String accessToken,
      String refreshToken
) {
}
