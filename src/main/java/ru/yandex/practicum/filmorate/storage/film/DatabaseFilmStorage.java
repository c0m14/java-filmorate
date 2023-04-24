package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotExistException;
import ru.yandex.practicum.filmorate.exception.WrongMpaRatingException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@Qualifier("DatabaseFilmStorage")
@Slf4j
@RequiredArgsConstructor
public class DatabaseFilmStorage implements FilmStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MpaRatingDao mpaRatingDao;

    @Override
    public Film addFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("FILM")
                .usingGeneratedKeyColumns("FILM_ID");

        Long filmId = simpleJdbcInsert.executeAndReturnKey(film.mapToDb()).longValue();

        if (!film.getGenres().isEmpty()) {

        }

        return getFilmById(filmId).orElseThrow(
                () -> new FilmNotExistException(String.format("Error while saving film %s", film))
                );
    }

    private void addGenreToFilm(int genreId) {
        String addFilmGenresSqlQuery = "INSERT INTO" +
                "FILM_GENRE(FILM_ID, GENRE_ID)" +
                "VALUES (:filmId, :genreId)";

    }

    @Override
    public Film updateFilm(Film film) {
        return null;
    }

    @Override
    public List<Film> getAllFilms() {
        return null;
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        String getFilmSqlQuery = "SELECT * FROM FILM " +
                "WHERE FILM_ID = :filmId";
        SqlParameterSource namedParam = new MapSqlParameterSource()
                .addValue("filmId", filmId);

        return Optional.ofNullable(
                jdbcTemplate.queryForObject(getFilmSqlQuery, namedParam, this::mapRowToFilm)
        );
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("FILM_ID"))
                .name(resultSet.getString("FILM_NAME"))
                .description(resultSet.getString("DESCRIPTION"))
                .releaseDate(resultSet.getDate("RELEASE_DATE").toLocalDate())
                .duration(resultSet.getInt("DURATION"))
                .mpa(mpaRatingDao.getMpaByIdFromDb(resultSet.getInt("MPA_RATING_ID")))
                .build();
    }
}
