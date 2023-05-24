package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ReviewDislikeNotExistsException;
import ru.yandex.practicum.filmorate.exception.ReviewLikeNotExistsException;
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
            filmFieldsValidator.checkIfPresentById(filmId);
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

        boolean isRecordFound = reviewStorage.removeLikeFromReview(reviewId, userId);
        if (!isRecordFound) {
            throw new ReviewLikeNotExistsException(
                    String.format("There is no like from user with id %d to review with id %d", userId, reviewId)
            );
        }
    }

    public void addDislikeToReview(Long reviewId, Long userId) {
        reviewFieldsValidator.checkIfPresentById(reviewId);
        userFieldsValidator.checkIfPresentById(userId);

        reviewStorage.addDislikeToReview(reviewId, userId);
    }

    public void removeDislikeFromReview(Long reviewId, Long userId) {
        reviewFieldsValidator.checkIfPresentById(reviewId);
        userFieldsValidator.checkIfPresentById(userId);

        boolean isRecordFound = reviewStorage.removeDislikeFromReview(reviewId, userId);
        if (!isRecordFound) {
            throw new ReviewDislikeNotExistsException(
                    String.format("There is no dislike from user with id %d to review with id %d", userId, reviewId)
            );
        }
    }

    public void deleteReview(Long reviewId) {
        reviewFieldsValidator.checkIfPresentById(reviewId);
        reviewStorage.deleteReview(reviewId);
    }

}
