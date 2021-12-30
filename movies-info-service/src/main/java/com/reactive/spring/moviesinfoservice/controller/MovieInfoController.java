package com.reactive.spring.moviesinfoservice.controller;

import java.util.Map;

import javax.validation.Valid;

import com.reactive.spring.moviesinfoservice.domain.MovieInfo;
import com.reactive.spring.moviesinfoservice.service.MovieInfoService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.*;

@RestController
@RequestMapping(MovieInfoController.MOVIE_INFO_CONTROLLER_PATH)
@RequiredArgsConstructor
public class MovieInfoController {

    public static final String MOVIE_INFO_CONTROLLER_PATH = "/v1/movies/info";

    private final MovieInfoService movieInfoService;

    private final Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().all();

    @GetMapping("/list")
    public Flux<MovieInfo> getAllMoviesInfo(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "name", required = false) String movieName) {

        if (year != null && movieName == null) {
            return movieInfoService.getMovieByYear(year).log();
        }
        if (year == null && movieName != null) {
            return movieInfoService.getMovieByName(movieName).log();
        }
        return movieInfoService.getAllMovieInfo().log();
    }

    @GetMapping("/{movieInfoId}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String movieInfoId) {
        return movieInfoService.getMovieInfoById(movieInfoId)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())).log();
    }

    @PostMapping("/add")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return movieInfoService.addMovieInfo(movieInfo)
                // publish that movie to something -> MovieInfoSink
                .doOnNext(movieInfoSink::tryEmitNext);
    }

    @GetMapping(path = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> getMovieInfoById() {
        // subscriber to this movie info
        return movieInfoSink.asFlux().log();
    }

    @PutMapping("/{movieInfoId}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo updatedMovieInfo,
            @PathVariable String movieInfoId) {
        return movieInfoService.updateMovieInfo(updatedMovieInfo, movieInfoId)
                .map(ResponseEntity.accepted()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();

    }

    @DeleteMapping("/{movieInfoId}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public Mono<Void> deleteMovieById(@PathVariable String movieInfoId) {
        return movieInfoService.deleteMovieInfoById(movieInfoId).log();
    }
}
