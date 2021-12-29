package com.reactivespring.config;

import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webclient(WebClient.Builder builder) {
        return builder.build();
    }

}
