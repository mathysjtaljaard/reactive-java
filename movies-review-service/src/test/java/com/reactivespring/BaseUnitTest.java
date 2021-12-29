package com.reactivespring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@AutoConfigureWebTestClient
@ActiveProfiles("unit-test")
public abstract class BaseUnitTest {

    @Autowired
    protected WebTestClient webTestClient;
}
