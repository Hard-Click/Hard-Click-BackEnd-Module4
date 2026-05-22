package com.wanted.backend.global.exception;

import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {

    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private String traceId;
    private Map<String, Object> details;


    private ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.traceId = MDC.get("traceId");
    }


    public static ErrorResponse create() {
        return new ErrorResponse();
    }


    public ErrorResponse errorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public ErrorResponse message(String message) {
        this.message = message;
        return this;
    }

    public ErrorResponse path(String path) {
        this.path = path;
        return this;
    }

    public ErrorResponse details(Map<String, Object> details) {
        this.details = details;
        return this;
    }


    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public String getTraceId() { return traceId; }
    public Map<String, Object> getDetails() { return details; }
}