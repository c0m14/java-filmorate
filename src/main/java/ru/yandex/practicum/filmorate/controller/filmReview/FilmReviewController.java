package ru.yandex.practicum.filmorate.controller.filmReview;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.service.filmReview.FilmReviewService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/reviews")
public class FilmReviewController {
    private final FilmReviewService filmReviewService;

    @PostMapping
    public FilmReview addReview(@Valid @RequestBody FilmReview filmReview) {
        log.debug("Got request to add review {} to film with id {} from user with id {}",
                filmReview, filmReview.getFilmId(), filmReview.getUserId());
        return filmReviewService.addReview(filmReview);
    }

    @PutMapping
    public FilmReview updateReview(@Valid @RequestBody FilmReview filmReview) {
        log.debug("Got request to update review {}", filmReview);

        return filmReviewService.updateReview(filmReview);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(
            @PathVariable(value = "id") Long reviewId
    ) {
        log.debug("Got request to delete review with id {}", reviewId);
        filmReviewService.deleteReview(reviewId);
    }

    @GetMapping("/{id}")
    public FilmReview getReviewById(
            @Valid
            @PathVariable("id") Long reviewId
    ) {
        log.debug("Got request to get review with id {}", reviewId);

        return filmReviewService.getReviewById(reviewId);
    }

    @GetMapping
    public List<FilmReview> getReviews(
            @RequestParam(value = "filmId", required = false) Long filmId,
            @RequestParam(value = "count", defaultValue = "10") @Min(1) int count
    ) {
        log.debug("Got request to get {} reviews to film with id {}", count, filmId);
        return filmReviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeToReview(
            @PathVariable(value = "id") Long reviewId,
            @PathVariable(value = "userId") Long userId
    ) {
        log.debug("Got request to add like to review with id {} from user with id {}", reviewId, userId);
        filmReviewService.addLikeToReview(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeFromReview(
            @PathVariable(value = "id") Long reviewId,
            @PathVariable(value = "userId") Long userId
    ) {
        log.debug("Got request to remove like from user with id {} from review with id {}", userId, reviewId);
        filmReviewService.removeLikeFromReview(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeToReview(
            @PathVariable(value = "id") Long reviewId,
            @PathVariable(value = "userId") Long userId
    ) {
        log.debug("Got request to add dislike to review with id {} from user with id {}", reviewId, userId);
        filmReviewService.addDislikeToReview(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDisLikeFromReview(
            @PathVariable(value = "id") Long reviewId,
            @PathVariable(value = "userId") Long userId
    ) {
        log.debug("Got request to remove dislike from user with id {} from review with id {}", userId, reviewId);
        filmReviewService.removeDislikeFromReview(reviewId, userId);
    }

}
