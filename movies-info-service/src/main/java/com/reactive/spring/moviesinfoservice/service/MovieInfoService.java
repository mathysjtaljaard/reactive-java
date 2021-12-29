package com.reactive.spring.moviesinfoservice.service;

import com.reactive.spring.moviesinfoservice.domain.MovieInfo;

import reactor.core.publisher.*;

public interface MovieInfoService {

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo);

    public Flux<MovieInfo> getAllMovieInfo();

    public Mono<MovieInfo> getMovieInfoById(String movieInfoId);

    public Flux<MovieInfo> getMovieByYear(Integer movieYear);

    public Flux<MovieInfo> getMovieByName(String name);

    public Mono<MovieInfo> updateMovieInfo(MovieInfo updatedMovieInfo, String movieInfoId);

    public Mono<Void> deleteMovieInfoById(String movieInfoId);
}
