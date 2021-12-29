package com.reactive.spring.moviesinfoservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.test.StepVerifier;

@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient
@ActiveProfiles("unit-test")
public class FluxAndMonoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void flux() {
        webTestClient.get().uri("/flux").exchange().expectStatus().is2xxSuccessful().expectBodyList(Integer.class)
                .hasSize(4);
    }

    @Test
    void fluxVerifyBody_ReturnedFlux() {
        var response = webTestClient.get().uri("/flux").exchange().expectStatus().is2xxSuccessful()
                .returnResult(Integer.class).getResponseBody();

        StepVerifier.create(response).expectNext(1, 2, 3, 4).verifyComplete();
    }

    @Test
    void fluxVerifyBodyFromRequest() {
        webTestClient.get().uri("/flux").exchange().expectStatus().is2xxSuccessful()
                .expectBodyList(Integer.class)
                .consumeWith(intergerBodyResponse -> {
                    var responseBody = intergerBodyResponse.getResponseBody();
                    assertEquals(4, responseBody.size());
                });
    }

    @Test
    void monoVerify() {
        webTestClient.get().uri("/mono").exchange().expectStatus().is2xxSuccessful().expectBody(String.class)
                .consumeWith(resultBody -> {
                    assertEquals("Hello World", resultBody.getResponseBody());
                });
    }

    @Test
    void streamVerify() {
        var response = webTestClient.get().uri("/stream").exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(response)
                .expectNext(0L, 1L, 2L, 3L, 4L, 5L, 6L)
                .thenCancel()
                .verify();
    }
}