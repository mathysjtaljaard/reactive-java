package com.reactivespring.exception;

public class ReviewsClientException extends RuntimeException {
    private final Integer code;

    public ReviewsClientException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
