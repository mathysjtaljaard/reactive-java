package com.reactivespring.router;

import java.util.function.Consumer;

import com.reactivespring.handler.ReviewHandler;

import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class ReviewRouter {

    private static final String API_VERSION = "/v1";

    public static final String REVIEW_PATH = API_VERSION + "/reviews";
    public static final String REVIEW_STREAM_PATH = "/stream";
    public static final String REVIEW_PATH_PARAMETER = "reviewId";
    public static final String REVIEW_QUERY_PARAMETER_MOVIE_INFO_ID = "movieInfoId";
    public static final String REVIEW_PATH_REVIEW_PATH_PARAMETER_PATTERN = "/{" + REVIEW_PATH_PARAMETER + "}";

    @Bean
    public RouterFunction<ServerResponse> reviewsRouter(ReviewHandler reviewHandler) {

        return RouterFunctions.route()
                .nest(RequestPredicates.path(REVIEW_PATH), builder -> builder
                        .GET(reviewHandler::getReviews)
                        .GET(REVIEW_STREAM_PATH, reviewHandler::getReviewsStream)
                        .POST(reviewHandler::addReview)
                        .PUT(REVIEW_PATH_REVIEW_PATH_PARAMETER_PATTERN, reviewHandler::updateReview)
                        .DELETE(REVIEW_PATH_REVIEW_PATH_PARAMETER_PATTERN, reviewHandler::deleteReview))
                .build();
    }
}
