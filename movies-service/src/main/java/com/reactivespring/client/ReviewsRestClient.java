package com.reactivespring.client;

import java.net.URI;

import javax.validation.constraints.NotEmpty;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.*;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.*;
import reactor.core.publisher.*;

@Component
@ConfigurationProperties(prefix = "rest-clients.movies-review")
@Validated
@RequiredArgsConstructor
@Data
public class ReviewsRestClient {

        private final WebClient webClient;

        @NotEmpty
        private String serviceUrl;

        public Flux<Review> retrieveReviews(String movieInfoId) {
                URI uri = UriComponentsBuilder
                                .fromHttpUrl(serviceUrl)
                                .queryParam("movieInfoId", movieInfoId).buildAndExpand()
                                .toUri();

                return webClient
                                .get()
                                .uri(uri)
                                .retrieve()
                                .onStatus(HttpStatus::is4xxClientError, ex -> {
                                        if (HttpStatus.NOT_FOUND.equals(ex.statusCode())) {
                                                return Mono.empty();
                                        }

                                        return ex.bodyToMono(String.class)
                                                        .flatMap(message -> Mono.error(new ReviewsClientException(
                                                                        message, ex.rawStatusCode())));
                                })
                                .onStatus(HttpStatus::is5xxServerError, ex -> {
                                        return ex.bodyToMono(String.class)
                                                        .flatMap(message -> Mono.error(new ReviewsServerException(
                                                                        "Reviews Rest Client Exception. Message -> "
                                                                                        + message)));
                                })
                                .bodyToFlux(Review.class).log();
        }
}
