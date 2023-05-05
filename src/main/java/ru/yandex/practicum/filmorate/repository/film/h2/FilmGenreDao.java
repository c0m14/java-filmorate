package ru.yandex.practicum.filmorate.repository.film.h2;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.GenreNotExistsException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
class FilmGenreDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void setGenresToFilm(Long filmId, Set<Integer> genresIds) {
        String sqlQuery = "MERGE INTO film_genre " +
                "VALUES ( :filmId, :genreId)";
        List<SqlParameterSource> namedParamsList = new ArrayList<>(genresIds.size());
        for (Integer genreId : genresIds) {
            MapSqlParameterSource namedParam = new MapSqlParameterSource()
                    .addValue("filmId", filmId)
                    .addValue("genreId", genreId);
            namedParamsList.add(namedParam);
        }

        try {
            jdbcTemplate.batchUpdate(sqlQuery, namedParamsList.toArray(SqlParameterSource[]::new));
        } catch (DataIntegrityViolationException e) {
            throw new GenreNotExistsException(
                    String.format("One of genres doesn't exist")
            );
        }

    }

    public void clearGenresFromFilm(Long filmId) {
        String sqlQuery = "DELETE " +
                "FROM film_genre " +
                "WHERE film_id = :filmId";
        SqlParameterSource namedParam = new MapSqlParameterSource("filmId", filmId);

        jdbcTemplate.update(sqlQuery, namedParam);
    }

    public Set<Genre> getGenresToFilm(Long filmId) {
        String sqlQuery = "SELECT genre_id, genre_name " +
                "FROM genre " +
                "WHERE genre_id IN " +
                "(SELECT genre_id " +
                "FROM film_genre " +
                "WHERE film_id = :filmId)";
        MapSqlParameterSource namedParams = new MapSqlParameterSource("filmId", filmId);
        List<Genre> filmGenres = jdbcTemplate.query(sqlQuery, namedParams, this::mapRowToGenre);

        return filmGenres.isEmpty() ? new HashSet<>() : new HashSet<>(filmGenres);
    }

    public Map<Long, Set<Genre>> getGenresToFilms(List<Long> filmsIds) {
        String sqlQuery = "SELECT fg.film_id, g.genre_id, g.genre_name " +
                "FROM genre AS g " +
                "RIGHT JOIN film_genre AS fg ON g.genre_id = fg.genre_id " +
                "WHERE g.genre_id IN " +
                "(SELECT genre_id " +
                "FROM film_genre " +
                "WHERE film_id IN (:filmIds))";
        SqlParameterSource namedParams = new MapSqlParameterSource("filmIds", filmsIds);
        Map<Long, Set<Genre>> filmsWithGenres = new HashMap<>();

        List<Map<Long, Genre>> filmIdWithGenreList = jdbcTemplate.query(sqlQuery, namedParams, ((rs, rowNum) -> {
            return Collections.singletonMap(
                    rs.getLong("film_id"),
                    Genre.builder()
                            .id(rs.getInt("genre_id"))
                            .name(rs.getString("genre_name"))
                            .build());
        }));

        filmIdWithGenreList.stream()
                .flatMap(map -> map.entrySet().stream())
                .forEach((entry -> {
                    if (filmsWithGenres.containsKey(entry.getKey())) {
                        filmsWithGenres.get(entry.getKey()).add(entry.getValue());
                    } else {
                        filmsWithGenres.put(entry.getKey(), Set.of(entry.getValue()));
                    }
                }
                ));

        return filmsWithGenres;

    }

    public Genre getGenreById(int id) {
        String sqlQuery = "SELECT genre_id, genre_name " +
                "FROM genre " +
                "WHERE genre_id = :genreId";
        SqlParameterSource namedParam = new MapSqlParameterSource("genreId", id);
        Genre genre;

        try {
            genre = jdbcTemplate.queryForObject(sqlQuery, namedParam, this::mapRowToGenre);
        } catch (EmptyResultDataAccessException e) {
            throw new GenreNotExistsException(
                    String.format("Genre with id %d doesn't exist", id)
            );
        }

        return genre;
    }

    public List<Genre> getAllGenres() {
        String sqlQuery = "SELECT genre_id, genre_name " +
                "FROM genre";

        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("genre_name"))
                .build();
    }
}
