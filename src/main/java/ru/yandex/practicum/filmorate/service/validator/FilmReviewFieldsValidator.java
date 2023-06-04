package ru.yandex.practicum.filmorate.service.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InvalidFieldsException;
import ru.yandex.practicum.filmorate.exception.NotExistsException;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.repository.filmReview.FilmReviewStorage;

@Component
@RequiredArgsConstructor
public class FilmReviewFieldsValidator {
    private final FilmReviewStorage filmReviewStorage;

    public void checkReviewId(Long id, RequestType requestType) {
        if (requestType.equals(RequestType.CREATE)) {
            if (id != null) {
                throw new InvalidFieldsException("FilmReview", "Id", "\"Id\" shouldn't be sent while creation");
            }
        } else if (requestType.equals(RequestType.UPDATE)) {
            if (id == null) {
                throw new InvalidFieldsException(
                        "FilmReview",
                        "id",
                        "\"Id\" shouldn't be empty in update request"
                );
            }
            checkIfPresentById(id);
        }
    }

    public void checkIfPresentById(Long reviewId) {
        if (filmReviewStorage.getReviewById(reviewId).isEmpty()) {
            throw new NotExistsException(
                    "Review",
                    String.format("Review with id %d does not exist", reviewId)
            );
        }
    }
}
