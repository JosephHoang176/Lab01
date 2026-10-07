package org.example.DTO.response;

import java.time.OffsetDateTime;

public record InvoiceResponse(long id, int orderId, String originalFilename,
                              String contentType, long sizeBytes, String sha256,
                              OffsetDateTime uploadedAt) {
}
