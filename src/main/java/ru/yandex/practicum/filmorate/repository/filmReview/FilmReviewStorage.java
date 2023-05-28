package ru.yandex.practicum.filmorate.repository.filmReview;

import ru.yandex.practicum.filmorate.model.FilmReview;

import java.util.List;
import java.util.Optional;

public interface FilmReviewStorage {

    FilmReview addReview(FilmReview filmReview);

    FilmReview updateReview(FilmReview filmReview);

    boolean deleteReview(Long reviewId);

    Optional<FilmReview> getReviewById(Long reviewId);

    List<FilmReview> getFilmReviews(Long filmId, int count);

    List<FilmReview> getAllReviews(int count);

    void addLikeToReview(Long reviewId, Long userId);

    boolean removeLikeFromReview(Long reviewId, Long userId);

    void addDislikeToReview(Long reviewId, Long userId);

    boolean removeDislikeFromReview(Long reviewId, Long userId);
}
