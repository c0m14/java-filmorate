package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ReviewNotExistsException;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.review.ReviewStorage;
import ru.yandex.practicum.filmorate.service.validator.FilmFieldsValidator;
import ru.yandex.practicum.filmorate.service.validator.ReviewFieldsValidator;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmFieldsValidator filmFieldsValidator;
    private final UserFieldsValidator userFieldsValidator;
    private final ReviewFieldsValidator reviewFieldsValidator;

    public Review addReview(Review review) {
        reviewFieldsValidator.checkReviewId(review.getReviewId(), RequestType.CREATE);
        filmFieldsValidator.checkIfPresentById(review.getFilmId());
        userFieldsValidator.checkIfPresentById(review.getUserId());

        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        reviewFieldsValidator.checkReviewId(review.getReviewId(), RequestType.UPDATE);

        return reviewStorage.updateReview(review);
    }

    public Review getReviewById(Long reviewId) {
        return reviewStorage.getReviewById(reviewId)
                .orElseThrow(
                        () -> new ReviewNotExistsException(
                                String.format("Review with id %d does not exist", reviewId)
                        ));
    }

    public List<Review> getReviews(Long filmId, int count) {
        if (filmId == null) {
            return reviewStorage.getAllReviews(count);
        } else {
            return reviewStorage.getFilmReviews(filmId, count);
        }
    }

    public void addLikeToReview(Long reviewId, Long userId) {
        reviewFieldsValidator.checkIfPresentById(reviewId);
        userFieldsValidator.checkIfPresentById(userId);

        reviewStorage.addLikeToReview(reviewId, userId);
    }

    public void removeLikeFromReview(Long reviewId, Long userId) {
        reviewFieldsValidator.checkIfPresentById(reviewId);
        userFieldsValidator.checkIfPresentById(userId);

        reviewStorage.removeLikeFromReview(reviewId, userId);
    }

    public void addDislikeToReview(Long reviewId, Long userId) {
        reviewFieldsValidator.checkIfPresentById(reviewId);
        userFieldsValidator.checkIfPresentById(userId);

        reviewStorage.addDislikeToReview(reviewId, userId);
    }

    public void removeDislikeFromReview(Long reviewId, Long userId) {
        reviewFieldsValidator.checkIfPresentById(reviewId);
        userFieldsValidator.checkIfPresentById(userId);

        reviewStorage.removeDislikeFromReview(reviewId, userId);
    }

    public void deleteReview(Long reviewId) {
        reviewFieldsValidator.checkIfPresentById(reviewId);
        reviewStorage.deleteReview(reviewId);
    }

}
