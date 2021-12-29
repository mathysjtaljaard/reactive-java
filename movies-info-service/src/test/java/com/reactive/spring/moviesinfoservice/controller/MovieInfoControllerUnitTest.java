package com.reactive.spring.moviesinfoservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import com.reactive.spring.moviesinfoservice.domain.MovieInfo;
import com.reactive.spring.moviesinfoservice.service.MovieInfoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.*;

@WebFluxTest(controllers = MovieInfoController.class)
@AutoConfigureWebTestClient
@ActiveProfiles("unit-test")
public class MovieInfoControllerUnitTest {

    private static final String movieInfoPath = MovieInfoController.MOVIE_INFO_CONTROLLER_PATH;
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoService;

    private MovieInfo getMovieInfo() {
        return MovieInfo.builder()
                .name("Yoda")
                .cast(List.of("granny", "apple"))
                .releaseDate(LocalDate.parse("2021-01-01"))
                .year(2021)
                .build();
    }

    @Test
    public void testGetAllMoviesEmptyList() {
        when(movieInfoService.getAllMovieInfo()).thenReturn(Flux.empty());

        webTestClient.get().uri(movieInfoPath + "/list")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(0);
    }

    @Test
    public void testGetAllMoviesTwoReturned() {
        when(movieInfoService.getAllMovieInfo()).thenReturn(Flux.just(getMovieInfo(), getMovieInfo()));

        webTestClient.get().uri(movieInfoPath + "/list")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(2);
    }

    @Test
    public void testGetMovieInfoById() {
        when(movieInfoService.getMovieInfoById(anyString())).thenReturn(Mono.just(getMovieInfo()));

        webTestClient.get().uri(movieInfoPath + "/{id}", "abcd")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("Yoda");
    }

    @Test
    public void testCreateMovieInfo() {

        when(movieInfoService.addMovieInfo(any(MovieInfo.class))).thenReturn(Mono.just(getMovieInfo()));

        webTestClient.post().uri(movieInfoPath + "/add")
                .bodyValue(getMovieInfo())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.year")
                .isEqualTo("2021");
    }

    @Test
    public void testCreateMovieInfo_ValidationNameValidationError() {

        MovieInfo info = getMovieInfo();
        info.setName("");
        info.setYear(null);
        info.setCast(null);

        webTestClient.post().uri(movieInfoPath + "/add")
                .bodyValue(info)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(response -> {
                    System.out.println(response.getResponseBodyContent());
                    List<String> errors = List.of(new String(response.getResponseBody()).split(","));
                    System.out.println(errors);
                    errors.stream().forEach(System.out::println);
                    assertEquals(4, errors.size());
                });

    }

    @Test
    public void testUpdateMovieInfo() {

        when(movieInfoService.updateMovieInfo(any(MovieInfo.class), anyString())).thenReturn(Mono.just(getMovieInfo()));

        webTestClient.put().uri(movieInfoPath + "/{id}", "123-movie")
                .bodyValue(getMovieInfo())
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.cast[0]")
                .isEqualTo("granny")
                .jsonPath("$.cast[1]")
                .isEqualTo("apple");
    }

    @Test
    public void testDeleteMovieInfo() {

        when(movieInfoService.deleteMovieInfoById(anyString())).thenReturn(Mono.empty());

        webTestClient.delete().uri(movieInfoPath + "/{id}", "123-movie")
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .isEmpty();
    }

}
