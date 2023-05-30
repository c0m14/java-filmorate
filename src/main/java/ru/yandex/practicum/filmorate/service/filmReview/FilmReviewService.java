package ru.yandex.practicum.filmorate.service.filmReview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmReviewDislikeNotExistsException;
import ru.yandex.practicum.filmorate.exception.FilmReviewLikeNotExistsException;
import ru.yandex.practicum.filmorate.exception.FilmReviewNotExistsException;
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.repository.filmReview.FilmReviewStorage;
import ru.yandex.practicum.filmorate.service.validator.FilmFieldsValidator;
import ru.yandex.practicum.filmorate.service.validator.FilmReviewFieldsValidator;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmReviewService {

    private final FilmReviewStorage filmReviewStorage;
    private final FilmFieldsValidator filmFieldsValidator;
    private final UserFieldsValidator userFieldsValidator;
    private final FilmReviewFieldsValidator filmReviewFieldsValidator;

    public FilmReview addReview(FilmReview filmReview) {
        filmReviewFieldsValidator.checkReviewId(filmReview.getReviewId(), RequestType.CREATE);
        filmFieldsValidator.checkIfPresentById(filmReview.getFilmId());
        userFieldsValidator.checkIfPresentById(filmReview.getUserId());

        return filmReviewStorage.addReview(filmReview);
    }

    public FilmReview updateReview(FilmReview filmReview) {
        filmReviewFieldsValidator.checkReviewId(filmReview.getReviewId(), RequestType.UPDATE);

        return filmReviewStorage.updateReview(filmReview);
    }

    public FilmReview getReviewById(Long reviewId) {
        return filmReviewStorage.getReviewById(reviewId)
                .orElseThrow(
                        () -> new FilmReviewNotExistsException(
                                String.format("Review with id %d does not exist", reviewId)
                        ));
    }

    public List<FilmReview> getReviews(Long filmId, int count) {
        if (filmId == null) {
            return filmReviewStorage.getAllReviews(count);
        } else {
            filmFieldsValidator.checkIfPresentById(filmId);
            return filmReviewStorage.getFilmReviews(filmId, count);
        }
    }

    public void addLikeToReview(Long reviewId, Long userId) {
        filmReviewFieldsValidator.checkIfPresentById(reviewId);
        userFieldsValidator.checkIfPresentById(userId);

        filmReviewStorage.addLikeToReview(reviewId, userId);
    }

    public void removeLikeFromReview(Long reviewId, Long userId) {
        filmReviewFieldsValidator.checkIfPresentById(reviewId);
        userFieldsValidator.checkIfPresentById(userId);

        boolean isRecordFound = filmReviewStorage.removeLikeFromReview(reviewId, userId);
        if (!isRecordFound) {
            throw new FilmReviewLikeNotExistsException(
                    String.format("There is no like from user with id %d to review with id %d", userId, reviewId)
            );
        }
    }

    public void addDislikeToReview(Long reviewId, Long userId) {
        filmReviewFieldsValidator.checkIfPresentById(reviewId);
        userFieldsValidator.checkIfPresentById(userId);

        filmReviewStorage.addDislikeToReview(reviewId, userId);
    }

    public void removeDislikeFromReview(Long reviewId, Long userId) {
        filmReviewFieldsValidator.checkIfPresentById(reviewId);
        userFieldsValidator.checkIfPresentById(userId);

        boolean isRecordFound = filmReviewStorage.removeDislikeFromReview(reviewId, userId);
        if (!isRecordFound) {
            throw new FilmReviewDislikeNotExistsException(
                    String.format("There is no dislike from user with id %d to review with id %d", userId, reviewId)
            );
        }
    }

    public void deleteReview(Long reviewId) {
        filmReviewFieldsValidator.checkIfPresentById(reviewId);
        filmReviewStorage.deleteReview(reviewId);
    }

}
