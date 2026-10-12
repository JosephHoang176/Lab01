package com.example.Web;

import com.example.Dto.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException exception,
                                                        HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException exception,
                                                          HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception,
                                                           HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message,
                                                HttpServletRequest request) {
        log.warn("HTTP request failed",
                StructuredArguments.keyValue("method", request.getMethod()),
                StructuredArguments.keyValue("path", request.getRequestURI()),
                StructuredArguments.keyValue("status", status.value()),
                StructuredArguments.keyValue("durationMs", 0));
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(), status.value(), status.getReasonPhrase(), message,
                request.getRequestURI(), org.slf4j.MDC.get(CorrelationIdFilter.MDC_KEY));
        return ResponseEntity.status(status).body(response);
    }
}
