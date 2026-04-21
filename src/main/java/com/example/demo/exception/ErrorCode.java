package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(999, "UNCATEGORIZED error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "INVALID message key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "User must be at least 3 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at 8 character", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    ;

    // Thêm trường statusCode vào Constructor
    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode; // Trường này cực kỳ quan trọng

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    // Thêm Getter này để JwtAuthenticationEntryPoint có thể gọi được
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}