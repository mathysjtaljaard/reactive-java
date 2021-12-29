package com.reactive.spring.moviesinfoservice.controller;

import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class RequestErrorHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleValidationException(WebExchangeBindException webe) {
        System.err.println("You hit the validation exception");
        return new ResponseEntity<>(webe.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .sorted()
                .collect(Collectors.joining(",")), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleNotFoundException(ResponseStatusException rse) {
        System.err.println("You hit the not found exception");
        return new ResponseEntity<>("Not Found " + rse.getMessage(), HttpStatus.NOT_FOUND);
    }
}
