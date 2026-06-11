package com.cryptosim.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 — Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    // 400 — Invalid Price
    @ExceptionHandler(InvalidPriceException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPrice(
            InvalidPriceException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_PRICE", ex.getMessage());
    }

    // 400 — Insufficient Balance
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(
            InsufficientBalanceException ex) {
        return build(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", ex.getMessage());
    }

    // 408 — Stale Price Data
    @ExceptionHandler(StaleDataException.class)
    public ResponseEntity<Map<String, Object>> handleStaleData(
            StaleDataException ex) {
        return build(HttpStatus.REQUEST_TIMEOUT, "STALE_PRICE_DATA", ex.getMessage());
    }

    // 403 — Access Denied (IDOR)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage());
    }

    // 404 — Not Found
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(
            RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    // 500 — Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR", ex.getMessage());
    }

    // Builder — standard error response format
    private ResponseEntity<Map<String, Object>> build(
            HttpStatus status, String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("code", code);
        body.put("message", message);
        body.put("status", status.value());
        return ResponseEntity.status(status).body(body);
    }
}