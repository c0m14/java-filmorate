package ru.yandex.practicum.filmorate.controller.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.Min;

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

    @GetMapping("/{id}")
    public Review getReviewById(
            @Valid
            @PathVariable("id") @Min(1) Long reviewId
    ) {
        log.debug("Got request to get review with id {}", reviewId);
        
        return reviewService.getReviewById(reviewId);
    }
}
