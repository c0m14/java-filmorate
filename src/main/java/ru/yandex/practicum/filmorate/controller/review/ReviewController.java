package ru.yandex.practicum.filmorate.controller.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.debug("Got request to add review {} to film with id {} from user with id {}",
                review, review.getFilmId(), review.getUserId());
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.debug("Got request to update review {}", review);

        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(
            @PathVariable(value = "id") Long reviewId
    ) {
        log.debug("Got request to delete review with id {}", reviewId);
        reviewService.deleteReview(reviewId);
    }

    @GetMapping("/{id}")
    public Review getReviewById(
            @Valid
            @PathVariable("id") Long reviewId
    ) {
        log.debug("Got request to get review with id {}", reviewId);

        return reviewService.getReviewById(reviewId);
    }

    @GetMapping
    public List<Review> getReviews(
            @RequestParam(value = "filmId", required = false) Long filmId,
            @RequestParam(value = "count", defaultValue = "10") @Min(1) int count
    ) {
        log.debug("Got request to get {} reviews to film with id {}", count, filmId);
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeToReview(
            @PathVariable(value = "id") Long reviewId,
            @PathVariable(value = "userId") Long userId
    ) {
        log.debug("Got request to add like to review with id {} from user with id {}", reviewId, userId);
        reviewService.addLikeToReview(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeFromReview(
            @PathVariable(value = "id") Long reviewId,
            @PathVariable(value = "userId") Long userId
    ) {
        log.debug("Got request to remove like from user with id {} from review with id {}", userId, reviewId);
        reviewService.removeLikeFromReview(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeToReview(
            @PathVariable(value = "id") Long reviewId,
            @PathVariable(value = "userId") Long userId
    ) {
        log.debug("Got request to add dislike to review with id {} from user with id {}", reviewId, userId);
        reviewService.addDislikeToReview(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDisLikeFromReview(
            @PathVariable(value = "id") Long reviewId,
            @PathVariable(value = "userId") Long userId
    ) {
        log.debug("Got request to remove dislike from user with id {} from review with id {}", userId, reviewId);
        reviewService.removeDislikeFromReview(reviewId, userId);
    }

}
