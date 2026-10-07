package org.example.Middleware;

import jakarta.servlet.http.HttpServletRequest;
import org.example.DTO.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse =
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getMessage(),
                        request.getRequestURI(),
                        request.getHeader(CORRELATION_ID_HEADER)
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException e,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse =
                new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        e.getMessage(),
                        request.getRequestURI(),
                        request.getHeader(CORRELATION_ID_HEADER)
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse =
                new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal server error",
                        request.getRequestURI(),
                        request.getHeader(CORRELATION_ID_HEADER)
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException e,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "Invoice file exceeds 10 MiB",
                request.getRequestURI(),
                request.getHeader(CORRELATION_ID_HEADER));
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }
}