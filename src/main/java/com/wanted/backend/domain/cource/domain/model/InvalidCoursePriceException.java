package com.wanted.backend.domain.cource.domain.model;

public class InvalidCoursePriceException extends RuntimeException {

    public InvalidCoursePriceException(String message) {
        super(message);
    }
}
