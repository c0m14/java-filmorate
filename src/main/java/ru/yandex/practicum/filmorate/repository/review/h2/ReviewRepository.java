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
import java.util.List;
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

    @Override
    public Optional<Review> getReviewById(Long reviewId) {
        String sqlQuery = "SELECT r.review_id, r.user_id, r.film_id, r.content, r.is_positive, " +
                "COUNT(likes.user_id) - COUNT(dislikes.user_id) AS useful " +
                "FROM review AS r " +
                "LEFT JOIN user_review_likes AS likes ON likes.review_id = r.review_id " +
                "LEFT JOIN user_review_dislikes AS dislikes ON dislikes.review_id = r.review_id " +
                "WHERE r.review_id = :reviewId " +
                "GROUP BY r.review_id ";
        MapSqlParameterSource namedParam = new MapSqlParameterSource("reviewId", reviewId);
        Optional<Review> reviewOptional;

        try {
            reviewOptional = Optional.ofNullable(
                    jdbcTemplate.queryForObject(sqlQuery, namedParam, this::mapRowToReviewWithUseful)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
        return reviewOptional;
    }

    @Override
    public List<Review> getFilmReviews(Long filmId, int count) {
        String sqlQuery = "SELECT r.review_id, r.user_id, r.film_id, r.content, r.is_positive, " +
                "COUNT(likes.user_id) - COUNT(dislikes.user_id) AS useful " +
                "FROM review AS r " +
                "LEFT JOIN user_review_likes AS likes ON likes.review_id = r.review_id " +
                "LEFT JOIN user_review_dislikes AS dislikes ON dislikes.review_id = r.review_id " +
                "WHERE r.film_id = :filmId " +
                "GROUP BY r.review_id " +
                "ORDER BY COUNT(likes.user_id) - COUNT(dislikes.user_id) DESC " +
                "LIMIT :count";
        MapSqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("count", count);

        try {
            return jdbcTemplate.query(sqlQuery, namedParams, this::mapRowToReviewWithUseful);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }

    }

    @Override
    public List<Review> getAllReviews(int count) {
        String sqlQuery = "SELECT r.review_id, r.user_id, r.film_id, r.content, r.is_positive, " +
                "COUNT(likes.user_id) - COUNT(dislikes.user_id) AS useful " +
                "FROM review AS r " +
                "LEFT JOIN user_review_likes AS likes ON likes.review_id = r.review_id " +
                "LEFT JOIN user_review_dislikes AS dislikes ON dislikes.review_id = r.review_id " +
                "GROUP BY r.review_id " +
                "ORDER BY COUNT(likes.user_id) - COUNT(dislikes.user_id) DESC " +
                "LIMIT :count";
        MapSqlParameterSource namedParams = new MapSqlParameterSource("count", count);

        try {
            return jdbcTemplate.query(sqlQuery, namedParams, this::mapRowToReviewWithUseful);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    @Override
    public void addLikeToReview(Long reviewId, Long userId) {
        String sqlQuery = "MERGE INTO user_review_likes " +
                "VALUES (:userId, :reviewId)";
        MapSqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("reviewId", reviewId);

        jdbcTemplate.update(sqlQuery, namedParams);
    }

    @Override
    public boolean removeLikeFromReview(Long reviewId, Long userId) {
        String sqlQuery = "DELETE FROM user_review_likes " +
                "WHERE review_id = :reviewId AND user_id = :userId";
        MapSqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("reviewId", reviewId);

        return jdbcTemplate.update(sqlQuery, namedParams) > 0;
    }

    @Override
    public void addDislikeToReview(Long reviewId, Long userId) {
        String sqlQuery = "MERGE INTO user_review_dislikes " +
                "VALUES (:userId, :reviewId)";
        MapSqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("reviewId", reviewId);

        jdbcTemplate.update(sqlQuery, namedParams);
    }

    @Override
    public boolean removeDislikeFromReview(Long reviewId, Long userId) {
        String sqlQuery = "DELETE FROM user_review_dislikes " +
                "WHERE review_id = :reviewId AND user_id = :userId";
        MapSqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("reviewId", reviewId);

        return jdbcTemplate.update(sqlQuery, namedParams) > 0;
    }

    @Override
    public boolean deleteReview(Long reviewId) {
        String sqlQuery = "DELETE FROM review " +
                "WHERE review_id = :reviewId";
        MapSqlParameterSource namedParam = new MapSqlParameterSource("reviewId", reviewId);

        return jdbcTemplate.update(sqlQuery, namedParam) > 0;
    }

    private Integer calculateUseful(Long reviewId) {
        String sqlQuery = "SELECT COUNT(likes.user_id) - COUNT(dislikes.user_id) " +
                "FROM user_review_likes AS likes " +
                "INNER JOIN user_review_dislikes AS dislikes ON likes.review_id = dislikes.review_id " +
                "WHERE likes.review_id = :reviewId AND dislikes.review_id = :reviewId " +
                "GROUP BY likes.review_id, dislikes.review_id";

        MapSqlParameterSource namedParam = new MapSqlParameterSource("reviewId", reviewId);

        try {
            return jdbcTemplate.queryForObject(sqlQuery, namedParam, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    private Review mapRowToReviewWithUseful(ResultSet resultSet, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(resultSet.getLong("review_id"))
                .userId(resultSet.getLong("user_id"))
                .filmId(resultSet.getLong("film_id"))
                .content(resultSet.getString("content"))
                .isPositive(resultSet.getBoolean("is_positive"))
                .useful(resultSet.getInt("useful"))
                .build();
    }

}
