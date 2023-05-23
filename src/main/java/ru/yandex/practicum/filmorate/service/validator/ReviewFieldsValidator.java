package ru.yandex.practicum.filmorate.service.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InvalidReviewFieldsException;
import ru.yandex.practicum.filmorate.exception.ReviewNotExistsException;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.repository.review.ReviewStorage;

@Component
@RequiredArgsConstructor
public class ReviewFieldsValidator {
    private final ReviewStorage reviewStorage;

    public void checkReviewId(Long id, RequestType requestType) {
        if (requestType.equals(RequestType.CREATE)) {
            if (id != null) {
                throw new InvalidReviewFieldsException("Id", "\"Id\" shouldn't be sent while creation");
            }
        } else if (requestType.equals(RequestType.UPDATE)) {
            if (id == null) {
                throw new InvalidReviewFieldsException(
                        "id",
                        "\"Id\" shouldn't be empty in update request"
                );
            }
            if (id <= 0) {
                throw new InvalidReviewFieldsException(
                        "id",
                        String.format("\"Id\" isn't positive: %d", id)
                );
            }
        }
    }

    public void checkIfPresentById(Long reviewId) {
        if (reviewStorage.getReviewById(reviewId).isEmpty()) {
            throw new ReviewNotExistsException(
                    String.format("Review with id %d does not exist", reviewId)
            );
        }
    }
}
