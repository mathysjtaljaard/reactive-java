package com.reactivespring.router;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.reactivespring.BaseIntegrationTest;
import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.*;

public class ReviewsIntegrationTest extends BaseIntegrationTest {

    private static final String REVIEW_PATH = ReviewRouter.REVIEW_PATH;
    private static final String REVIEW_WITH_PATH_PARAM_REVIEWID = REVIEW_PATH
            + ReviewRouter.REVIEW_PATH_REVIEW_PATH_PARAMETER_PATTERN;
    private static final String REVIEW_QUERY_PARAM_MOVIE_INFO_ID = ReviewRouter.REVIEW_QUERY_PARAMETER_MOVIE_INFO_ID;

    @Autowired
    private ReviewReactiveRepository reactiveRepository;

    @BeforeEach
    void setUp() {
        List<Review> reviews = List.of(
                new Review(null, "1", "Awesome Movie", 9.0),
                new Review("12345asdfg", "1", "Awesome Movie1", 9.0),
                new Review(null, "2", "Awesome Movie2", 9.0));

        reactiveRepository.saveAll(reviews).blockLast();
    }

    @AfterEach
    void teardown() {
        reactiveRepository.deleteAll().block();
    }

    @Test
    public void addReview() {
        System.out.println("web test client -> " + webTestClient);

        Review badReview = new Review(null, "2", "This was just junk", 1.0);
        webTestClient.post()
                .uri(REVIEW_PATH)
                .bodyValue(badReview)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(review -> {
                    Review savedReview = review.getResponseBody();
                    System.out.println(savedReview);
                    assertAll(
                            () -> assertNotNull(savedReview),
                            () -> assertNotNull(savedReview.getReviewId()));
                });
    }

    @Test
    public void getReviews() {

        webTestClient.get().uri(REVIEW_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .consumeWith(serverResponse -> {
                    List<Review> reviews = serverResponse.getResponseBody();
                    assert reviews != null;
                    assertAll(
                            () -> assertEquals(3, reviews.size()));

                });
    }

    @Test
    public void updateReview() {

        webTestClient
                .put()
                .uri(REVIEW_WITH_PATH_PARAM_REVIEWID, "12345asdfg")
                .bodyValue(new Review(null, "1", "Oh, no, what did I just watch", 2.0))
                .exchange()
                .expectStatus()
                .isAccepted()
                .expectBody(Review.class)
                .consumeWith(serverResponse -> {
                    Review review = serverResponse.getResponseBody();
                    assert review != null;

                    assertAll(
                            () -> assertEquals("Oh, no, what did I just watch", review.getComment()),
                            () -> assertEquals(2.0, review.getRating()));
                });
    }

    @Test
    public void updateReview_NotFound() {

        webTestClient
                .put()
                .uri(REVIEW_WITH_PATH_PARAM_REVIEWID, "NotReal")
                .bodyValue(new Review(null, "1", "Oh, no, what did I just watch", 2.0))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .consumeWith(serverResponse -> {
                    String error = serverResponse.getResponseBody();
                    assert error != null;
                    assertEquals("Review not found for the given review Id NotReal", error);
                });
    }

    @Test
    public void deleteReview() {
        webTestClient.delete()
                .uri(REVIEW_WITH_PATH_PARAM_REVIEWID, "12345asdfg")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void getReviewByMovieId() {

        UriComponents uri = UriComponentsBuilder.fromUriString(REVIEW_PATH)
                .queryParam(REVIEW_QUERY_PARAM_MOVIE_INFO_ID, 1L)
                .buildAndExpand();
        webTestClient
                .get()
                .uri(uri.toUri())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .consumeWith(serverResponse -> {
                    List<Review> reviews = serverResponse.getResponseBody();
                    assert reviews != null;
                    assert reviews.size() == 2;
                });
    }
}
