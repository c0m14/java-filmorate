package ru.yandex.practicum.filmorate.repository.film.h2;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
 class FilmLikesDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Long getFilmLikes(Long filmId) {
        String countFilmLikesSqlQuery = "SELECT COUNT(user_id) " +
                "FROM user_film_likes " +
                "WHERE film_id = :filmId " +
                "GROUP BY film_id";
        SqlParameterSource namedParam = new MapSqlParameterSource("filmId", filmId);
        Long likes = 0L;

        try {
            likes = jdbcTemplate.queryForObject(countFilmLikesSqlQuery, namedParam, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return likes;
        }

        return likes;
    }
}
