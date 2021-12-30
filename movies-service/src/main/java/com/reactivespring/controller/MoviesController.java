package com.reactivespring.controller;

import java.util.*;
import java.util.stream.Collectors;

import javax.print.attribute.standard.Media;

import com.reactivespring.client.*;
import com.reactivespring.domain.*;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.*;
import reactor.util.function.Tuple2;

@RestController
@RequestMapping(MoviesController.MOVIES_PATH)
@RequiredArgsConstructor
public class MoviesController {

    private static final String API_VERSION = "/v1";
    public static final String MOVIES_PATH = API_VERSION + "/movies";

    private final MovieInfoRestClient movieInfoRestClient;
    private final ReviewsRestClient reviewsRestClient;

    @GetMapping(value = "/{movieInfoId}")
    public Mono<Movie> retrieveMovieById(@PathVariable String movieInfoId) {

        return movieInfoRestClient.retriveMovieInfo(movieInfoId)
                .flatMap(movieInfo -> reviewsRestClient.retrieveReviews(movieInfoId)
                        .collectList()
                        .map(reviews -> new Movie(movieInfo, reviews)))
                .switchIfEmpty(Mono.empty());

    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Movie> retrieveMovieInfoStream() {

        return movieInfoRestClient.retrieveMovieInfoStream()
                .flatMap(
                        movieInfo -> reviewsRestClient.retrieveReviews(movieInfo.getMovieInfoId())
                                .collectList()
                                .map(reviews -> new Movie(movieInfo, reviews)));
    }

}
