package com.reactivespring.handler;

import java.util.*;
import java.util.stream.Collectors;

import javax.validation.*;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.*;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.*;

@RequiredArgsConstructor
@Slf4j
@Component
public class ReviewHandler {

    private final ReviewReactiveRepository reviewReactiveRepository;
    private final Validator validator;

    private static final String MOVIE_INFO_ID_QUERY_PARAM = ReviewRouter.REVIEW_QUERY_PARAMETER_MOVIE_INFO_ID;
    private static final String REVIEW_ID_PATH_VARIABLE = ReviewRouter.REVIEW_PATH_PARAMETER;

    private void validate(Review review) {
        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        log.info("Violitions {}", violations);

        if (!violations.isEmpty()) {
            throw new ReviewDataException(violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(", ")));
        }
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class).log()
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save).log()
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue).log();
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {

        Optional<String> movieId = request.queryParam(MOVIE_INFO_ID_QUERY_PARAM);

        Flux<Review> reviews;

        if (movieId.isPresent()) {
            reviews = reviewReactiveRepository.findByMovieInfoId(movieId.get()).log();
        } else {
            reviews = reviewReactiveRepository.findAll();
        }

        return reviews.hasElements().flatMap(hasElements -> {
            if (Boolean.TRUE.equals(hasElements)) {
                return ServerResponse.ok().body(reviews, Review.class);
            }

            String message = movieId.isPresent() ? "No Reviews found for movie info id " + movieId.get()
                    : "No Reviews found";
            return Mono.error(new ReviewNotFoundException(message));
        });
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {

        String reviewId = request.pathVariable(REVIEW_ID_PATH_VARIABLE);

        return reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(
                        Mono.error(new ReviewNotFoundException("Review not found for the given review Id " + reviewId)))
                .flatMap(review -> request.bodyToMono(Review.class).log()
                        .map(updatedReview -> {
                            review.setComment(updatedReview.getComment());
                            review.setRating(updatedReview.getRating());
                            return review;
                        }))
                .log()
                .flatMap(reviewReactiveRepository::save).log()
                .flatMap(ServerResponse.status(HttpStatus.ACCEPTED)::bodyValue).log();
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {

        return reviewReactiveRepository.deleteById(request.pathVariable(REVIEW_ID_PATH_VARIABLE)).log()
                .then(ServerResponse.noContent().build()).log();
    }

}
