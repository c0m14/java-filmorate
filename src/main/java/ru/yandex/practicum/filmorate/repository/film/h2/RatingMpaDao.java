package ru.yandex.practicum.filmorate.repository.film.h2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.MpaRatingNotExistException;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.util.Optional;

@Component
@RequiredArgsConstructor
 class RatingMpaDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RatingMPA getMpaByIdFromDb(int mpaId) {
        if (mpaId == 0) {
            return null;
        }

        String sqlQuery = "SELECT mpa_rating_name FROM mpa_rating " +
                "WHERE mpa_rating_id = :mpaRatingId";
        SqlParameterSource namedParam = new MapSqlParameterSource("mpaRatingId", mpaId);

        Optional<String> mpaName = Optional.ofNullable(
                jdbcTemplate.queryForObject(sqlQuery, namedParam, String.class)
        );

        return new RatingMPA(
                mpaId,
                mpaName.orElseThrow(() -> new MpaRatingNotExistException(
                        String.format("There is no Mpa Rating for id: %n", mpaId))
                )
        );
    }

    public void setRatingMpaToFilm(Long filmId, int ratingMpaId) {
        String sqlQuery = "MERGE INTO film (film_id, mpa_rating_id) " +
                "KEY (film_id) " +
                "VALUES (:filmId, :ratingMpaId) ";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("ratingMpaId", ratingMpaId)
                .addValue("filmId", filmId);

        try {
            jdbcTemplate.update(sqlQuery, namedParams);
        } catch (DataIntegrityViolationException e) {
            throw new MpaRatingNotExistException(
                    String.format("Mpa rating with id %d doesn't exist", ratingMpaId)
            );
        }
    }
}
