package com.reactive.spring.moviesinfoservice.service;

import com.reactive.spring.moviesinfoservice.domain.MovieInfo;
import com.reactive.spring.moviesinfoservice.repository.MovieInfoRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.*;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieInfoService {

    private final MovieInfoRepository movieInfoRepository;

    @Override
    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return movieInfoRepository.save(movieInfo);
    }

    @Override
    public Flux<MovieInfo> getAllMovieInfo() {
        return movieInfoRepository.findAll();
    }

    @Override
    public Mono<MovieInfo> getMovieInfoById(String movieInfoId) {
        return movieInfoRepository.findById(movieInfoId);
    }

    @Override
    public Mono<MovieInfo> updateMovieInfo(MovieInfo updatedMovieInfo, String movieInfoId) {

        return movieInfoRepository.findById(movieInfoId)
                .flatMap(movieInfo -> {
                    movieInfo.setCast(updatedMovieInfo.getCast());
                    movieInfo.setName(updatedMovieInfo.getName());
                    movieInfo.setReleaseDate(updatedMovieInfo.getReleaseDate());
                    movieInfo.setYear(updatedMovieInfo.getYear());
                    return movieInfoRepository.save(movieInfo);
                });
    }

    @Override
    public Mono<Void> deleteMovieInfoById(String movieInfoId) {
        return movieInfoRepository.deleteById(movieInfoId);

    }

    @Override
    public Flux<MovieInfo> getMovieByYear(Integer movieYear) {
        return movieInfoRepository.findByYear(movieYear);
    }

    @Override
    public Flux<MovieInfo> getMovieByName(String movieName) {
        return movieInfoRepository.findByName(movieName);
    }
}
