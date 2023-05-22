package ru.yandex.practicum.filmorate.repository.review.h2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.review.ReviewStorage;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReviewRepository implements ReviewStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Review addReview(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("review")
                .usingGeneratedKeyColumns("review_id");

        Long reviewId = simpleJdbcInsert.executeAndReturnKey(review.mapToDb()).longValue();
        review.setReviewId(reviewId);

        return review;
    }
}
