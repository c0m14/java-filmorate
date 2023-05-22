package ru.yandex.practicum.filmorate.repository.review.h2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ReviewNotExistsException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.review.ReviewStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;


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

    @Override
    @Transactional
    public Review updateReview(Review review) {
        Long reviewId = review.getReviewId();
        String sqlQuery = "UPDATE review " +
                "SET content = :content, is_positive = :isPositive " +
                "WHERE review_id = :reviewId";
        MapSqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("isPositive", review.getIsPositive())
                .addValue("reviewId", review.getReviewId());

        try {
            jdbcTemplate.update(sqlQuery, namedParams);
        } catch (DataIntegrityViolationException e) {
            throw new ReviewNotExistsException(
                    String.format("Review with id %d does not exist", review.getReviewId())
            );
        }

        //Возвращаем отзыв из БД, так как в полученном могут быть некорректные поля
        Review reviewFromDb = getReviewById(reviewId).get();
        reviewFromDb.setUseful(calculateUseful(reviewId));

        return reviewFromDb;
    }

    public Optional<Review> getReviewById(Long reviewId) {
        String sqlQuery = "SELECT review_id, user_id, film_id, content, is_positive " +
                "FROM review " +
                "WHERE review_id = :reviewId";
        MapSqlParameterSource namedParam = new MapSqlParameterSource("reviewId", reviewId);
        Optional<Review> reviewOptional;

        try {
            reviewOptional = Optional.ofNullable(
                    jdbcTemplate.queryForObject(sqlQuery, namedParam, this::mapRowToReview)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
        return reviewOptional;
    }

    private Integer calculateUseful(Long reviewId) {
        Integer likes = 0;
        Integer dislikes = 0;
        String likesSqlQuery = "SELECT COUNT(user_id) " +
                "FROM user_review_likes " +
                "WHERE review_id = :reviewId " +
                "GROUP BY review_id";
        String dislikesSqlQuery = "SELECT COUNT(user_id) " +
                "FROM user_review_dislikes " +
                "WHERE review_id = :reviewId " +
                "GROUP BY review_id";
        MapSqlParameterSource namedParam = new MapSqlParameterSource("reviewId", reviewId);

        try {
            likes = jdbcTemplate.queryForObject(likesSqlQuery, namedParam, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Likes not found for review with id {}", reviewId);
        }

        try {
            dislikes = jdbcTemplate.queryForObject(dislikesSqlQuery, namedParam, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Dislikes not found for review with id {}", reviewId);
        }

        return likes - dislikes;
    }

    private Review mapRowToReview(ResultSet resultSet, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(resultSet.getLong("review_id"))
                .userId(resultSet.getLong("user_id"))
                .filmId(resultSet.getLong("film_id"))
                .content(resultSet.getString("content"))
                .isPositive(resultSet.getBoolean("is_positive"))
                .build();
    }

}
