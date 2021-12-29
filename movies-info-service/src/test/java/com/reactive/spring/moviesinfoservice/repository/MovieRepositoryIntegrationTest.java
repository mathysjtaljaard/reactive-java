package com.reactive.spring.moviesinfoservice.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import com.reactive.spring.moviesinfoservice.domain.MovieInfo;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;

@DataMongoTest
@ActiveProfiles("unit-test")
public class MovieRepositoryIntegrationTest {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    private MovieInfo movie1 = MovieInfo.builder().name("Welcome 1").cast(List.of("bob", "jill"))
            .releaseDate(LocalDate.parse("2021-01-01")).year(2021).build();
    private MovieInfo movie2 = MovieInfo.builder().name("Welcome 2").cast(List.of("bob2", "jill2"))
            .releaseDate(LocalDate.parse("2021-02-01")).year(2022).build();
    private MovieInfo movie3 = MovieInfo.builder().movieInfoId("rds").name("Welcome 3").cast(List.of("bob3", "jill3"))
            .releaseDate(LocalDate.parse("2021-03-01")).year(2023).build();

    @BeforeEach
    public void setUp() {
        // Need to blocklast due to the async nature
        movieInfoRepository.saveAll(List.of(movie1, movie2, movie3)).blockLast();
    }

    @AfterEach
    public void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    public void findAll() {

        StepVerifier.create(movieInfoRepository.findAll().log())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void findById() {

        StepVerifier.create(movieInfoRepository.findById("rds").log())
                .assertNext(movie -> {
                    assertEquals("Welcome 3", movie.getName());
                })
                .verifyComplete();
    }

    @Test
    public void saveMovie() {

        var movie = MovieInfo.builder().cast(List.of("randy", "carly")).movieInfoId("customId").name("Well Who Knows")
                .releaseDate(LocalDate.parse(("2021-03-03"))).year(3232).build();
        StepVerifier.create(movieInfoRepository.save(movie).log())
                .assertNext(movieResponse -> {
                    assertEquals("Well Who Knows", movieResponse.getName());
                })
                .verifyComplete();
    }

    @Test
    public void updateMovieInfo() {

        var movie = movieInfoRepository.findById("rds").block();
        movie.setName("Custom Name ");

        StepVerifier.create(movieInfoRepository.save(movie).log())
                .assertNext(movieResponse -> {
                    assertEquals("Custom Name ", movieResponse.getName());
                })
                .verifyComplete();
    }

    @Test
    public void deleteMovie() {

        movieInfoRepository.deleteById("rds").block();

        StepVerifier.create(movieInfoRepository.findById("rds").log())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void findMovieByYear() {
        StepVerifier.create(movieInfoRepository.findByYear(2021).log())
                .assertNext(movie -> {
                    assertAll("movieDetails",
                            () -> assertEquals(movie.getName(), movie1.getName()),
                            () -> assertEquals(movie.getYear(), movie1.getYear()));
                })
                .verifyComplete();
    }

    @Test
    public void findMovieByName() {
        StepVerifier.create(movieInfoRepository.findByName("Welcome 1").log())
                .assertNext(movie -> {
                    assertAll("movieDetails",
                            () -> assertEquals(movie.getName(), movie1.getName()),
                            () -> assertEquals(movie.getYear(), movie1.getYear()));
                })
                .verifyComplete();
    }
}
