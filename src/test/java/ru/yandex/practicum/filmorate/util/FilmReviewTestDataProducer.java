package ru.yandex.practicum.filmorate.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.repository.filmReview.FilmReviewStorage;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FilmReviewTestDataProducer {
    private final FilmReviewStorage filmReviewStorage;
    private final TestDataProducer testDataProducer;

    public FilmReview getValidPositiveReview() {
        Long userId = testDataProducer.addDefaultUserToDB();
        Long filmId = testDataProducer.addDefaultFilmToDB();

        return FilmReview.builder()
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
            filmReviewStorage.addReview(
                    FilmReview.builder()
                            .filmId(filmId)
                            .userId(userId)
                            .content("Some content")
                            .isPositive(true)
                            .build()
            );
        }

        List<FilmReview> filmReviews = filmReviewStorage.getFilmReviews(filmId, reviewsNumber);

        for (FilmReview filmReview : filmReviews) {
            for (Long usersId : usersIds) {
                filmReviewStorage.addLikeToReview(filmReview.getReviewId(), usersId);
            }
            usersIds.remove(usersIds.size() - 1);
        }

        return filmId;
    }

}
