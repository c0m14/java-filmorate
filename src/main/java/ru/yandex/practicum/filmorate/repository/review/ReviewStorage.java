package ru.yandex.practicum.filmorate.repository.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review addReview(Review review);
    Review updateReview(Review review);
    Optional<Review> getReviewById(Long reviewId);
    List<Review> getFilmReviews(Long filmId, int count);
    List<Review> getAllReviews(int count);
    void addLikeToReview(Long reviewId, Long userId);
    boolean removeLikeFromReview(Long reviewId, Long userId);
    void addDislikeToReview(Long reviewId, Long userId);
    boolean removeDislikeFromReview(Long reviewId, Long userId);
}
