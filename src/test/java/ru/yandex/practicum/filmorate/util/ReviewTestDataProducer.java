package ru.yandex.practicum.filmorate.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.review.ReviewStorage;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewTestDataProducer {
    private final ReviewStorage reviewStorage;
    private final TestDataProducer testDataProducer;

    public Review getValidPositiveReview() {
        Long userId = testDataProducer.addDefaultUserToDB();
        Long filmId = testDataProducer.addDefaultFilmToDB();

        return Review.builder()
                .filmId(filmId)
                .userId(userId)
                .content("Good")
                .isPositive(true)
                .build();
    }

    //Useful уменьшается с ростом reviewId
    public Long createReviewsWithUsefulToFilmAndReturnFilmId(int reviewsNumber) {
        Long filmId = testDataProducer.addDefaultFilmToDB();
        List<Long> usersIds = new ArrayList<>(reviewsNumber);
        for (int i = 0; i < reviewsNumber; i++) {
            usersIds.add(
                    testDataProducer.addDefaultUserToDB()
            );
        }

        for (Long userId : usersIds) {
            reviewStorage.addReview(
                    Review.builder()
                            .filmId(filmId)
                            .userId(userId)
                            .content("Some content")
                            .isPositive(true)
                            .build()
            );
        }

        List<Review> reviews = reviewStorage.getFilmReviews(filmId, reviewsNumber);

        for (Review review : reviews) {
            for (Long usersId : usersIds) {
                reviewStorage.addLikeToReview(review.getReviewId(), usersId);
            }
            usersIds.remove(usersIds.size() - 1);
        }

        return filmId;
    }

}
