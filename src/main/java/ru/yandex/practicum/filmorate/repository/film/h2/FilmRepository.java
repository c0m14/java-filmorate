package ru.yandex.practicum.filmorate.repository.film.h2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@Qualifier("H2FilmRepository")
@Slf4j
@RequiredArgsConstructor
public class FilmRepository implements FilmStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RatingMpaDao ratingMpaDao;
    private final FilmGenreDao filmGenreDao;
    private final FilmLikesDao filmLikesDao;

    @Override
    @Transactional
    public Film addFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("film")
                .usingGeneratedKeyColumns("film_id");

        Long filmId = simpleJdbcInsert.executeAndReturnKey(film.mapToDb()).longValue();

        //отдельно лениво и в рамках транзакции добавляем зависящие от БД поля
        if (film.getMpa() != null) {
            ratingMpaDao.setRatingMpaToFilm(filmId, film.getMpa().getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.getGenres().stream()
                    .mapToInt(Genre::getId)
                    .forEach(genreId -> filmGenreDao.setGenreToFilm(filmId, genreId));
        }

        film.setId(filmId);
        return film;
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
    public Optional<Film> getFilmByIdFull(Long filmId) {
        //Конструируем фильм с базовыми полями
        Optional<Film> filmOptional = getFilmByIdLite(filmId);

        //Добавляем имя для MPA Rating
        filmOptional.ifPresent(film -> film.setMpa(ratingMpaDao.getMpaByIdFromDb(film.getMpa().getId())));

        //Добавляем жанры
        filmOptional.ifPresent(film -> film.setGenres(filmGenreDao.getGenresToFilm(filmId)));

        //Добавляем лайки
        filmOptional.ifPresent(film -> film.setLikesCount(filmLikesDao.getFilmLikes(filmId)));

        return filmOptional;
    }

    //Возвращает фильм с базовыми полями из таблицы film
    public Optional<Film> getFilmByIdLite(Long filmId) {
        String getFilmSqlQuery = "SELECT * FROM film " +
                "WHERE film_id = :filmId";
        SqlParameterSource namedParam = new MapSqlParameterSource()
                .addValue("filmId", filmId);

        Optional<Film> filmOptional;
        try {
            filmOptional = Optional.ofNullable(
                    jdbcTemplate.queryForObject(getFilmSqlQuery, namedParam, this::mapRowToFilm)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

        return filmOptional;
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("film_id"))
                .name(resultSet.getString("film_name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .mpa(RatingMPA.builder()
                        .id(resultSet.getInt("mpa_rating_id"))
                        .build()
                )
                .build();
    }
}
