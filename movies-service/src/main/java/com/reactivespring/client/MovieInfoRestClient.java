package com.reactivespring.client;

import javax.validation.constraints.NotEmpty;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.*;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.*;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "rest-clients.movies-info")
@Validated
@Data
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
                    return ex.bodyToMono(String.class)
                            .flatMap(message -> Mono.error(new MoviesInfoServerException(
                                    "Movie Info Rest Client Exception. Message -> " + message)));
                })
                .bodyToMono(MovieInfo.class)
                .log();
    }
}
