package ru.yandex.practicum.filmorate.service.validator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InvalidReviewFieldsException;
import ru.yandex.practicum.filmorate.model.RequestType;

@Component
public class ReviewFieldsValidator {

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
}
