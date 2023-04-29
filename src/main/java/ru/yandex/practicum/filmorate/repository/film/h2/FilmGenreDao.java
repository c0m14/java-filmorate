package ru.yandex.practicum.filmorate.repository.film.h2;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.GenreNotExistsException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
 class FilmGenreDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void setGenreToFilm(Long filmId, int genreId) {
        String sqlQuery ="MERGE INTO film_genre " +
                "VALUES ( :filmId, :genreId)";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("genreId", genreId);
        try {
            jdbcTemplate.update(sqlQuery, namedParams);
        } catch (DataIntegrityViolationException e) {
            throw new GenreNotExistsException(
                 String.format("Genre with id %d doesn't exist", genreId)
            );
        }

    }

    public Set<Genre> getGenresToFilm(Long filmId) {
        String sqlQuery = "SELECT genre_id, genre_name " +
                "FROM genre " +
                "WHERE genre_id IN " +
                "(SELECT genre_id " +
                "FROM film_genre " +
                "WHERE film_id = :filmId)";
        MapSqlParameterSource namedParams = new MapSqlParameterSource("filmId", filmId);
        List<Genre> filmGenres = jdbcTemplate.query(sqlQuery, namedParams, this::MapRowToGenre);

        return filmGenres.isEmpty() ? new HashSet<>() : new HashSet<>(filmGenres);
    }

    private Genre MapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("genre_name"))
                .build();
    }
}
