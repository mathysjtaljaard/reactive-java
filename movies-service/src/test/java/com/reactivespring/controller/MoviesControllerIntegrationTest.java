package com.reactivespring.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.base.BaseIntegrationTest;
import com.reactivespring.domain.*;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.spec.internal.HttpHeaders;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import io.netty.handler.codec.http.HttpResponseStatus;

@AutoConfigureWireMock(port = 8084)
public class MoviesControllerIntegrationTest extends BaseIntegrationTest {

        private static final String MOVIES_PATH = MoviesController.MOVIES_PATH;
        private static final String MOVIE_INFO_ID = "1";

        private Movie getMovie() {
                List<Review> reviews = List.of(new Review("1", "1", "comment", 2.2),
                                new Review("2", "1", "comment2", 2.2));
                MovieInfo info = new MovieInfo("1", "Jokes", 2021, List.of("bob"), LocalDate.parse("2021-12-12"));
                return new Movie(info, reviews);
        }

        @Test
        void testRetrieveMovieById() throws JsonProcessingException {

                String movieInfoJSON = mapper.writeValueAsString(getMovie().getMovieInfo());
                String movieReviewsJSON = mapper.writeValueAsString(getMovie().getReviewList());

                stubFor(
                                get(urlEqualTo("/v1/movies/info/" + MOVIE_INFO_ID))
                                                .willReturn(
                                                                aResponse()
                                                                                .withHeader(HttpHeaders.CONTENT_TYPE,
                                                                                                ContentType.APPLICATION_JSON
                                                                                                                .toString())
                                                                                .withBody(movieInfoJSON)));

                stubFor(
                                get(urlEqualTo("/v1/reviews?movieInfoId=" + MOVIE_INFO_ID))
                                                .willReturn(
                                                                aResponse()
                                                                                .withHeader(HttpHeaders.CONTENT_TYPE,
                                                                                                ContentType.APPLICATION_JSON
                                                                                                                .toString())
                                                                                .withBody(movieReviewsJSON)));

                webTestClient
                                .get()
                                .uri(MOVIES_PATH + "/{movieInfoId}", MOVIE_INFO_ID)
                                .exchange()
                                .expectStatus()
                                .isOk()
                                .expectBody(Movie.class)
                                .consumeWith(response -> {
                                        Movie movie = response.getResponseBody();
                                        assert movie != null;
                                        assertAll(
                                                        () -> {
                                                                assert movie.getReviewList().size() == 2;
                                                        },
                                                        () -> {
                                                                assert "Jokes".equalsIgnoreCase(
                                                                                movie.getMovieInfo().getName());
                                                        });
                                });
        }

        @Test
        public void testRetieveMovieById_4xxResponseErrors_MovieInfoService() {

                stubFor(
                                get(urlEqualTo("/v1/movies/info/" + MOVIE_INFO_ID))
                                                .willReturn(
                                                                aResponse()
                                                                                .withStatus(HttpResponseStatus.NOT_FOUND
                                                                                                .code())));
                webTestClient.get()
                                .uri(MOVIES_PATH + "/{movieInfoId}", MOVIE_INFO_ID)
                                .exchange()
                                .expectStatus()
                                .isNotFound()
                                .expectBody(String.class)
                                .consumeWith(result -> {
                                        System.out.println(result.getResponseBody());
                                        assert ("No movie information found for movie info id : " + MOVIE_INFO_ID)
                                                        .equals(result.getResponseBody());
                                });

                WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movies/info/" + MOVIE_INFO_ID)));
        }

        @Test
        public void testRetieveMovieById_4xxResponseErrors_ReviewService() throws JsonProcessingException {

                String movieInfoJSON = mapper.writeValueAsString(getMovie().getMovieInfo());

                stubFor(
                                get(urlEqualTo("/v1/movies/info/" + MOVIE_INFO_ID))
                                                .willReturn(
                                                                aResponse()
                                                                                .withHeader(HttpHeaders.CONTENT_TYPE,
                                                                                                ContentType.APPLICATION_JSON
                                                                                                                .toString())
                                                                                .withBody(movieInfoJSON)));

                stubFor(
                                get(urlEqualTo("/v1/reviews?movieInfoId=" + MOVIE_INFO_ID))
                                                .willReturn(
                                                                aResponse()
                                                                                .withStatus(HttpResponseStatus.NOT_FOUND
                                                                                                .code())));

                webTestClient.get()
                                .uri(MOVIES_PATH + "/{movieInfoId}", MOVIE_INFO_ID)
                                .exchange()
                                .expectStatus()
                                .isOk()
                                .expectBody(Movie.class)
                                .consumeWith(response -> {
                                        Movie movie = response.getResponseBody();
                                        assert movie != null;
                                        assertAll(
                                                        () -> {
                                                                assert movie.getReviewList() != null;
                                                        },
                                                        () -> {
                                                                assert movie.getReviewList().size() == 0;
                                                        },
                                                        () -> {
                                                                assert "Jokes".equalsIgnoreCase(
                                                                                movie.getMovieInfo().getName());
                                                        });
                                });
        }

        @Test
        public void testRetieveMovieById_5xxResponseErrors_MovieInfoService() {

                stubFor(
                                get(urlEqualTo("/v1/movies/info/" + MOVIE_INFO_ID))
                                                .willReturn(
                                                                aResponse()
                                                                                .withStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR
                                                                                                .code())
                                                                                .withBody("Movie Service Unavailable")));
                webTestClient.get()
                                .uri(MOVIES_PATH + "/{movieInfoId}", MOVIE_INFO_ID)
                                .exchange()
                                .expectStatus()
                                .is5xxServerError()
                                .expectBody(String.class)
                                .consumeWith(result -> {
                                        assert "Movie Info Rest Client Exception. Message -> Movie Service Unavailable"
                                                        .equals(result.getResponseBody());
                                });

                // WireMock verify used with getRequested for will test the stub endpoint count
                WireMock.verify(4, WireMock.getRequestedFor(urlEqualTo("/v1/movies/info/" + MOVIE_INFO_ID)));
        }

        @Test
        public void testRetieveMovieById_5xxResponseErrors_ReviewService() throws JsonProcessingException {

                String movieInfoJSON = mapper.writeValueAsString(getMovie().getMovieInfo());

                stubFor(
                                get(urlEqualTo("/v1/movies/info/" + MOVIE_INFO_ID))
                                                .willReturn(
                                                                aResponse()
                                                                                .withHeader(HttpHeaders.CONTENT_TYPE,
                                                                                                ContentType.APPLICATION_JSON
                                                                                                                .toString())
                                                                                .withBody(movieInfoJSON)));

                stubFor(
                                get(urlEqualTo("/v1/reviews?movieInfoId=" + MOVIE_INFO_ID))
                                                .willReturn(
                                                                aResponse()
                                                                                .withStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR
                                                                                                .code())
                                                                                .withBody("Review Service Unavailable")));

                webTestClient.get()
                                .uri(MOVIES_PATH + "/{movieInfoId}", MOVIE_INFO_ID)
                                .exchange()
                                .expectStatus()
                                .is5xxServerError()
                                .expectBody(String.class)
                                .consumeWith(result -> {
                                        assert "Reviews Rest Client Exception. Message -> Review Service Unavailable"
                                                        .equals(result.getResponseBody());
                                });

                // WireMock verify used with getRequested for will test the stub endpoint count
                WireMock.verify(4, WireMock.getRequestedFor(urlPathMatching("/v1/reviews*")));
        }
}
