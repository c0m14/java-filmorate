package ru.yandex.practicum.filmorate.repository.film.h2;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotExistsException;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RatingMpaDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RatingMPA getMpaByIdFromDb(int mpaId) {
        String sqlQuery = "SELECT mpa_rating_id, mpa_rating_name FROM mpa_rating " +
                "WHERE mpa_rating_id = :mpaRatingId";
        SqlParameterSource namedParam = new MapSqlParameterSource("mpaRatingId", mpaId);
        RatingMPA ratingMPA;

        try {
            ratingMPA = jdbcTemplate.queryForObject(sqlQuery, namedParam, this::mapRowToMpa);
        } catch (EmptyResultDataAccessException e) {
            throw new NotExistsException(
                    "Mpa rating",
                    String.format("Mpa rating with id %d does not exist", mpaId)
            );
        }

        return ratingMPA;
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
            throw new NotExistsException(
                    "Mpa rating",
                    String.format("Mpa rating with id %d does not exist", ratingMpaId)
            );
        }
    }

    public List<RatingMPA> getAllMpa() {
        String sqlQuery = "SELECT mpa_rating_id, mpa_rating_name " +
                "FROM mpa_rating";

        return jdbcTemplate.query(sqlQuery, this::mapRowToMpa);
    }

    private RatingMPA mapRowToMpa(ResultSet resultSet, int rowNum) throws SQLException {
        return new RatingMPA(
                resultSet.getInt("mpa_rating_id"),
                resultSet.getString("mpa_rating_name")
        );
    }
}
