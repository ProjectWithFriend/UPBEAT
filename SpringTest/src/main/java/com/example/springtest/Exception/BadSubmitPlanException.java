package com.example.springtest.Exception;

public class BadSubmitPlanException extends RuntimeException{
    public BadSubmitPlanException(String message) {
        super(message);
    }

    public BadSubmitPlanException(String message, Throwable cause) {
        super(message, cause);
    }
}
