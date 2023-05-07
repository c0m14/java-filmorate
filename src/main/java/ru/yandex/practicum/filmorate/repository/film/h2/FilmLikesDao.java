package ru.yandex.practicum.filmorate.repository.film.h2;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
class FilmLikesDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Long getFilmLikes(Long filmId) {
        String sqlQuery = "SELECT COUNT(user_id) " +
                "FROM user_film_likes " +
                "WHERE film_id = :filmId " +
                "GROUP BY film_id";
        SqlParameterSource namedParam = new MapSqlParameterSource("filmId", filmId);
        Long likes = 0L;

        try {
            likes = jdbcTemplate.queryForObject(sqlQuery, namedParam, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return likes;
        }

        return likes;
    }

    public Map<Long, Long> getFilmsLikes(List<Long> filmIds) {
        String sqlQuery = "SELECT film_id, COUNT(user_id) " +
                "FROM user_film_likes " +
                "WHERE film_id IN (:filmIds) " +
                "GROUP BY film_id";
        Map<Long, Long> filmIdsMapToCountLikes = new HashMap<>();

        SqlParameterSource namedParam = new MapSqlParameterSource("filmIds", filmIds);

        List<Map<Long, Long>> filmsIdsLikesCountList = jdbcTemplate.query(sqlQuery, namedParam, (rs, rowNum) -> {
            return Collections.singletonMap(
                    rs.getLong("film_id"),
                    rs.getLong("COUNT(user_id)")
            );
        });

        if (filmsIdsLikesCountList.isEmpty()) {
            return Map.of();
        }

        filmsIdsLikesCountList.stream()
                .flatMap(map -> map.entrySet().stream())
                .forEach((entry -> {
                    filmIdsMapToCountLikes.put(entry.getKey(), entry.getValue());
                }
                ));
        return filmIdsMapToCountLikes;
    }

    public void setFilmLike(Long filmId, Long userId) {
        String sqlQuery = "INSERT INTO user_film_likes " +
                "VALUES (:userId, :filmId)";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("filmId", filmId);

        jdbcTemplate.update(sqlQuery, namedParams);
    }

    public boolean removeFilmLike(Long filmId, Long userId) {
        String sqlQuery = "DELETE FROM user_film_likes " +
                "WHERE film_id = :filmId AND user_id = :userId";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);

        return jdbcTemplate.update(sqlQuery, namedParams) > 0;
    }

}
