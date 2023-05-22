package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.review.ReviewStorage;
import ru.yandex.practicum.filmorate.service.validator.FilmFieldsValidator;
import ru.yandex.practicum.filmorate.service.validator.ReviewFieldsValidator;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmFieldsValidator filmFieldsValidator;
    private final UserFieldsValidator userFieldsValidator;
    private final ReviewFieldsValidator reviewFieldsValidator;

    public Review addReview (Review review) {
        reviewFieldsValidator.checkReviewId(review.getReviewId(), RequestType.CREATE);
        filmFieldsValidator.checkIfPresentById(review.getFilmId());
        userFieldsValidator.checkIfPresentById(review.getUserId());

        return reviewStorage.addReview(review);
    }
}
