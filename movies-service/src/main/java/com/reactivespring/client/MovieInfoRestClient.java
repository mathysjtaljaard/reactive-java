package com.reactivespring.client;

import javax.validation.constraints.NotEmpty;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.*;
import com.reactivespring.util.RetryUtil;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.*;

@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "rest-clients.movies-info")
@Validated
@Data
@Slf4j
public class MovieInfoRestClient {

    @NotEmpty
    private String serviceUrl;

    private final WebClient webclient;

    public Mono<MovieInfo> retriveMovieInfo(String movieInfoId) {

        return webclient.get().uri(serviceUrl.concat("/{movieInfoId}"), movieInfoId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, ex -> {
                    if (HttpStatus.NOT_FOUND.equals(ex.statusCode())) {
                        String message = "No movie information found for movie info id : ";
                        return Mono.error(
                                new MoviesInfoClientException(message.concat(movieInfoId), ex.rawStatusCode()));
                    }

                    return ex.bodyToMono(String.class)
                            .flatMap(message -> Mono.error(new MoviesInfoClientException(message, ex.rawStatusCode())));
                })
                .onStatus(HttpStatus::is5xxServerError, ex -> {
                    log.info("Status of 5xx");
                    return ex.bodyToMono(String.class)
                            .flatMap(message -> Mono.error(new MoviesInfoServerException(
                                    "Movie Info Rest Client Exception. Message -> " + message)));
                })
                .bodyToMono(MovieInfo.class)
                .retryWhen(RetryUtil.retrySpec())
                .log();
    }

    public Flux<MovieInfo> retrieveMovieInfoStream() {

        return webclient
                .get().uri(serviceUrl.concat("/stream"))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, ex -> {
                    return ex.bodyToMono(String.class)
                            .flatMap(message -> Mono.error(new MoviesInfoClientException(message, ex.rawStatusCode())));
                })
                .onStatus(HttpStatus::is5xxServerError, ex -> {
                    log.info("Status of 5xx");
                    return ex.bodyToMono(String.class)
                            .flatMap(message -> Mono.error(new MoviesInfoServerException(
                                    "Movie Info Rest Client Exception. Message -> " + message)));
                })
                .bodyToFlux(MovieInfo.class)
                .retryWhen(RetryUtil.retrySpec())
                .log();
    }
}
