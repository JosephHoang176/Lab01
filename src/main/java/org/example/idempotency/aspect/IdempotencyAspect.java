package org.example.idempotency.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.idempotency.IdemAnnotation.Idempotent;
import org.example.idempotency.IdempotencyRecordStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Aspect
@Component
public class IdempotencyAspect {

    private final IdempotencyRecordStore recordStore;
    private final ObjectMapper objectMapper;

    public IdempotencyAspect(IdempotencyRecordStore recordStore, ObjectMapper objectMapper) {
        this.recordStore = recordStore;
        this.objectMapper = objectMapper;
    }

    @Around(value = "@annotation(idempotent)", argNames = "joinPoint,idempotent")
    public Object handleIdempotent(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        String key = request.getHeader("Idempotency-Key");
        if (key == null || key.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Idempotency-Key header");
        }

        if (key.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key is too long");
        }

        Principal principal = request.getUserPrincipal();
        String ownerKey = principal == null ? "anonymous" : principal.getName();
        String method = request.getMethod();
        String path = request.getRequestURI();
        long ttl = idempotent.expiredIn();

        boolean firstRequest = recordStore.tryClaim(key, method, path, ownerKey, ttl);
        if (!firstRequest) {
            IdempotencyRecordStore.IdempotencyRecord record =
                    recordStore.find(key, method, path, ownerKey).orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.CONFLICT, "Request is being processed")
                    );
            if ("PROCESSING".equals(record.status())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Your request is being processed. Please wait."
                );
            }
            if ("COMPLETED".equals(record.status()) && record.responseBody() != null) {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                return objectMapper.readValue(record.responseBody(), signature.getReturnType());
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid idempotency record");
        }

        try {
            Object result = joinPoint.proceed();
            String jsonResult = objectMapper.writeValueAsString(result);
            recordStore.complete(key, method, path, ownerKey, jsonResult);
            return result;
        } catch (Throwable throwable) {
            recordStore.release(key, method, path, ownerKey);
            throw throwable;
        }
    }
}
