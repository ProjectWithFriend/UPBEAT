package com.example.springtest.HandleExceptions;

import com.example.springtest.Exception.BadRequestException;
import com.example.springtest.Exception.BadSubmitPlanException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class CustomExceptionHandler {

    /*
    * Handle BadRequestException
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {
        Map<String,Object> response = Map.of("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BadSubmitPlanException.class)
    public ResponseEntity<Object> handleBadSubmitPlan(BadSubmitPlanException ex) {
        Map<String,Object> response = Map.of("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
