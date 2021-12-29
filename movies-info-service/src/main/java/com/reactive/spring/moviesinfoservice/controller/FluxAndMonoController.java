package com.reactive.spring.moviesinfoservice.controller;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.*;

@RestController
public class FluxAndMonoController {

    @GetMapping("/flux")
    public Flux<Integer> getFlux() {
        return Flux.just(1, 2, 3, 4).log();
    }

    @GetMapping("/mono")
    public Mono<String> getMono() {
        return Mono.just("Hello World").log();
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> getStream() {
        return Flux.interval(Duration.ofSeconds(1)).log();
    }
}
