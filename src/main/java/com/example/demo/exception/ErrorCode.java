package com.example.demo.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(999," UNCATEGORIZED error"),
    INVALID_KEY(1001,"INVALID message key"),
    USER_EXISTED(  1002 ,  "User existed"),
    USERNAME_INVALID( 1003,"User must be at least 3 characters"),

    INVALID_PASSWORD(1004,"Password must be at 8 character"),
    USER_NOT_EXISTED(  1005 ,  "User not existed"),
    UNAUTHENTICATED(1006,"Unauthenticated"),
    ;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private  int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
