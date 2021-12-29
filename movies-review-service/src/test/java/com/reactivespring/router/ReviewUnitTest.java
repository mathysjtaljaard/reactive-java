package com.reactivespring.router;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.*;

import com.reactivespring.BaseUnitTest;
import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalExceptionHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.*;

@ContextConfiguration(classes = { ReviewRouter.class, ReviewHandler.class, GlobalExceptionHandler.class })
public class ReviewUnitTest extends BaseUnitTest {

    private static final String REVIEW_PATH = ReviewRouter.REVIEW_PATH;
    private static final String REVIEW_WITH_PATH_PARAM_REVIEWID = REVIEW_PATH
            + ReviewRouter.REVIEW_PATH_REVIEW_PATH_PARAMETER_PATTERN;
    private static final String REVIEW_QUERY_PARAM_MOVIE_INFO_ID = ReviewRouter.REVIEW_QUERY_PARAMETER_MOVIE_INFO_ID;

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Test
    public void testAddReview() {

        Review prePost = new Review(null, "10", "This was OK", 5.0);
        Review expected = new Review();
        BeanUtils.copyProperties(prePost, expected);
        expected.setReviewId("1234Created");

        when(reviewReactiveRepository.save(any(Review.class))).thenReturn(Mono.just(expected));

        webTestClient.post()
                .uri(REVIEW_PATH)
                .bodyValue(prePost)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(response -> {
                    Review newReview = response.getResponseBody();
                    System.out.println("New Review -> " + newReview);
                    assertNotNull(newReview);
                    assertEquals(expected, newReview);
                });
    }

    @Test
    public void testAddReviewNegativeRating() {

        Review review = new Review(null, null, "This Should Fail", -2.3);

        webTestClient.post().uri(REVIEW_PATH)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String message = response.getResponseBody();
                    assert message != null;

                    List<String> errors = Arrays.asList(message.split(","));

                    assertAll(
                            () -> assertEquals(2, errors.size()),
                            () -> assertEquals("movieInfoId : must not be null", errors.get(0)),
                            () -> assertEquals(
                                    " rating.negative : rating is negative and please pass a non-negative value",
                                    errors.get(1)));

                });
    }

    @Test
    public void getAllReviews() {

        Flux<Review> reviewList = Flux.just(new Review("1", "1", "Movie 1", 7.5), new Review("2", "2", "Movie 2", 8.0));
        when(reviewReactiveRepository.findAll()).thenReturn(reviewList);

        webTestClient
                .get()
                .uri(REVIEW_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .consumeWith(response -> {
                    List<Review> reviews = response.getResponseBody();

                    assert reviews != null;
                    assertEquals(2, reviews.size());
                });
    }

    @Test
    public void getAllReviewsEmptyResponse() {

        when(reviewReactiveRepository.findAll()).thenReturn(Flux.empty());

        webTestClient
                .get()
                .uri(REVIEW_PATH)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void updateReview() {
        Review preUpdate = new Review("1up", "4", "This was ", 5.0);
        Review postUpdate = new Review();
        BeanUtils.copyProperties(preUpdate, postUpdate);
        postUpdate.setComment("This was good");
        postUpdate.setMovieInfoId("3");

        when(reviewReactiveRepository.findById("1up")).thenReturn(Mono.just(preUpdate));
        when(reviewReactiveRepository.save(preUpdate)).thenReturn(Mono.just(postUpdate));

        webTestClient
                .put()
                .uri(REVIEW_WITH_PATH_PARAM_REVIEWID, "1up")
                .bodyValue(postUpdate)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(Review.class)
                .consumeWith(response -> {
                    Review updated = response.getResponseBody();

                    assert updated != null;
                    assertAll(
                            () -> assertEquals(postUpdate.getMovieInfoId(), updated.getMovieInfoId()),
                            () -> assertEquals(postUpdate.getComment(), updated.getComment()));
                });

    }

    @Test
    public void getReviewsByMovieInfoId() {
        Flux<Review> reviewList = Flux.just(new Review("1", "1", "Movie 1", 7.5), new Review("2", "1", "Movie 2", 8.0));
        when(reviewReactiveRepository.findByMovieInfoId("1")).thenReturn(reviewList);

        webTestClient
                .get()
                .uri(
                        UriComponentsBuilder.fromPath(REVIEW_PATH)
                                .queryParam(REVIEW_QUERY_PARAM_MOVIE_INFO_ID, 1L)
                                .build()
                                .toUri())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .consumeWith(response -> {
                    List<Review> reviews = response.getResponseBody();
                    assert reviews != null;
                    assert reviews.size() == 2;
                });
    }

    @Test
    public void getReviewsByMovieInfoId_NoneReturned() {

        when(reviewReactiveRepository.findByMovieInfoId("1")).thenReturn(Flux.empty());

        webTestClient
                .get()
                .uri(
                        UriComponentsBuilder.fromPath(REVIEW_PATH)
                                .queryParam(REVIEW_QUERY_PARAM_MOVIE_INFO_ID, 1L)
                                .build()
                                .toUri())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void deleteReview() {

        when(reviewReactiveRepository.deleteById(any(String.class))).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(REVIEW_WITH_PATH_PARAM_REVIEWID, "12345")
                .exchange()
                .expectStatus().isNoContent();
    }
}
