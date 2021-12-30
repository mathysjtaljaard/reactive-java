package com.reactive.spring.moviesinfoservice.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.*;
import com.reactive.spring.moviesinfoservice.domain.MovieInfo;
import com.reactive.spring.moviesinfoservice.repository.MovieInfoRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.*;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("unit-test")
public class MovieInfoControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    @BeforeEach
    public void setUp() {
        var movie1 = MovieInfo.builder().name("Welcome 1").cast(List.of("bob", "jill"))
                .releaseDate(LocalDate.parse("2021-01-01")).year(2021).build();
        var movie2 = MovieInfo.builder().name("Welcome 2").cast(List.of("bob2", "jill2"))
                .releaseDate(LocalDate.parse("2021-02-01")).year(2022).build();
        var movie3 = MovieInfo.builder().movieInfoId("rds").name("Welcome 3").cast(List.of("bob3", "jill3"))
                .releaseDate(LocalDate.parse("2021-03-01")).year(2023).build();

        // Need to blocklast due to the async nature
        movieInfoRepository.saveAll(List.of(movie1, movie2, movie3)).blockLast();
    }

    @AfterEach
    public void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    public void addMovieInfo_ValidData() throws Exception {

        MovieInfo infoToAdd = MovieInfo.builder()
                .cast(List.of("Jenny", "David"))
                .name("Crack on a Stick")
                .releaseDate(LocalDate.parse("2022-01-01"))
                .year(2022)
                .build();

        String requestData = mapper.writeValueAsString(infoToAdd);

        webTestClient.post()
                .uri("/v1/movies/info/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestData)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .consumeWith(movie -> {
                    System.out.println(movie);
                    try {
                        MovieInfo movieInfo = mapper.readValue(movie.getResponseBody(), MovieInfo.class);
                        System.out.println(movieInfo);
                    } catch (StreamReadException e) {
                        e.printStackTrace();
                    } catch (DatabindException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

    }

    @Test
    public void addMovieInfo_AddMovieInfoId_FailValidation() throws Exception {

        MovieInfo infoToAdd = MovieInfo.builder()
                .movieInfoId("123456")
                .cast(List.of("Jenny", "David"))
                .releaseDate(LocalDate.parse("2022-01-01"))
                .year(2022)
                .build();

        String requestData = mapper.writeValueAsString(infoToAdd);

        webTestClient.post()
                .uri("/v1/movies/info/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestData)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(response -> {
                    System.out.println(response);
                });
    }

    @Test
    public void getAllMovies() throws Exception {

        webTestClient.get()
                .uri("/v1/movies/info/list")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    public void getMovieByYear() throws Exception {

        UriComponents uri = UriComponentsBuilder.fromUriString("/v1/movies/info/list")
                .queryParam("year", 2021)
                .buildAndExpand();
        webTestClient.get()
                .uri(uri.toUri())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    public void getMovieByName() throws Exception {

        UriComponents uri = UriComponentsBuilder.fromUriString("/v1/movies/info/list")
                .queryParam("name", "Welcome 1")
                .buildAndExpand();

        webTestClient.get()
                .uri(uri.toUri())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    public void getMovieInfoById() throws Exception {
        webTestClient.get()
                .uri("/v1/movies/info/rds")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieReturned -> {
                    assertNotNull(movieReturned.getResponseBody());
                });
    }

    @Test
    public void getMovieInfoByIdDiffValidationApproachJSON() throws Exception {
        webTestClient.get()
                .uri("/v1/movies/info/rds")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("Welcome 3");
    }

    @Test
    public void updateMovieInfo() throws Exception {

        MovieInfo infoToAdd = MovieInfo.builder()
                .cast(List.of("Brandy", "Sasha"))
                .name("On a stick")
                .releaseDate(LocalDate.parse("2023-01-01"))
                .year(2023)
                .build();

        String requestData = mapper.writeValueAsString(infoToAdd);

        webTestClient.put()
                .uri("/v1/movies/info/rds")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestData)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .consumeWith(movie -> {
                    System.out.println(movie);
                    try {
                        MovieInfo movieInfo = mapper.readValue(movie.getResponseBody(), MovieInfo.class);
                        System.out.println(movieInfo);
                        assertEquals("On a stick", movieInfo.getName());
                    } catch (StreamReadException e) {
                        e.printStackTrace();
                    } catch (DatabindException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });

    }

    @Test
    public void deleteMovieById() throws Exception {
        webTestClient.delete()
                .uri("/v1/movies/info/rds")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().isEmpty();

        webTestClient.get()
                .uri("/v1/movies/info/rds")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }

    @Test
    public void getMovieInfoById_NotFound() throws Exception {
        webTestClient.get()
                .uri("/v1/movies/info/notFound")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void updateMovieInfo_notFound() throws Exception {

        MovieInfo infoToAdd = MovieInfo.builder()
                .cast(List.of("Brandy", "Sasha"))
                .name("On a stick")
                .releaseDate(LocalDate.parse("2023-01-01"))
                .year(2023)
                .build();

        String requestData = mapper.writeValueAsString(infoToAdd);

        webTestClient.put()
                .uri("/v1/movies/info/bando")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestData)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void getAllMoviesStream() throws Exception {

        MovieInfo infoToAdd = MovieInfo.builder()
                .movieInfoId("123456")
                .name("Jack the Ripper")
                .cast(List.of("Jenny", "David"))
                .releaseDate(LocalDate.parse("2022-01-01"))
                .year(2022)
                .build();

        String requestData = mapper.writeValueAsString(infoToAdd);

        webTestClient.post()
                .uri("/v1/movies/info/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestData)
                .exchange()
                .expectStatus().isCreated();

        Flux<MovieInfo> responseFlux = webTestClient.get()
                .uri("/v1/movies/info/stream")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .getResponseBody();

        StepVerifier.create(responseFlux).assertNext(movieInfo -> {
            assert movieInfo.getMovieInfoId() != null;
        })
                .thenCancel()
                .verify();
    }
}
