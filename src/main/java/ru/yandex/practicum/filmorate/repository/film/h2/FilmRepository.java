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
import java.util.ArrayList;
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

        if (!film.getGenres().isEmpty()) {
            film.getGenres().stream()
                    .mapToInt(Genre::getId)
                    .forEach(genreId -> filmGenreDao.setGenreToFilm(filmId, genreId));
        }

        film.setId(filmId);
        return film;
    }

    @Override
    @Transactional
    public Film updateFilm(Film film) {
        String sqlQuery = "UPDATE film " +
                "SET film_name = :filmName, description = :description, release_date = :releaseDate, " +
                "duration = :duration " +
                "WHERE film_id = :filmId";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("filmName", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("mpaRatingId", film.getMpa().getId())
                .addValue("filmId", film.getId());

        jdbcTemplate.update(sqlQuery, namedParams);

        if (film.getMpa() != null) {
            ratingMpaDao.setRatingMpaToFilm(film.getId(), film.getMpa().getId());
        }

        if (!film.getGenres().isEmpty()) {
            film.getGenres().stream()
                    .mapToInt(Genre::getId)
                    .forEach(genreId -> filmGenreDao.setGenreToFilm(film.getId(), genreId));
        }

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String sqlQuery = "SELECT film_id, film_name, description, release_date, duration, mpa_rating_id " +
                "FROM film ";

       List<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
       films.forEach(this::fetchAdditionalParams);
       return films;
    }

    @Override
    public Optional<Film> getFilmByIdFull(Long filmId) {
        //Конструируем фильм с базовыми полями
        Optional<Film> filmOptional = getFilmByIdLite(filmId);

        //Добавляем дополнительные параметры
        filmOptional.ifPresent(this::fetchAdditionalParams);

        return filmOptional;
    }

    @Override
    public void giveLikeFromUserToFilm(Long filmId, Long userId) {
        filmLikesDao.setFilmLike(filmId, userId);
    }

    @Override
    public boolean removeUserLikeFromFilm(Long filmId, Long userId) {
        return filmLikesDao.removeFilmLike(filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                "f.mpa_rating_id " +
                "FROM film AS f " +
                "LEFT JOIN user_film_likes AS likes ON f.film_id = likes.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY SUM(likes.user_id) DESC " +
                "LIMIT :count";
        SqlParameterSource namedParam = new MapSqlParameterSource("count", count);
        List<Film> films = new ArrayList<>(count);

        try {
            films = jdbcTemplate.query(sqlQuery, namedParam, this::mapRowToFilm);
        } catch (EmptyResultDataAccessException e) {
            return films;
        }

        films.forEach(this::fetchAdditionalParams);
        return films;

    }

    @Override
    public RatingMPA getMpaById(int mapId) {
        return ratingMpaDao.getMpaByIdFromDb(mapId);
    }

    @Override
    public List<RatingMPA> getAllMpa() {
        return ratingMpaDao.getAllMpa();
    }

    private void fetchAdditionalParams (Film film) {
        fetchRatingMpa(film);
        fetchGenres(film);
        fetchLikes(film);
    }

    private void fetchRatingMpa(Film film) {
        film.setMpa(ratingMpaDao.getMpaByIdFromDb(film.getMpa().getId()));
    }

    private void fetchGenres(Film film) {
        film.setGenres(filmGenreDao.getGenresToFilm(film.getId()));
    }

    private void fetchLikes(Film film) {
        film.setLikesCount(filmLikesDao.getFilmLikes(film.getId()));
    }

    //Возвращает фильм с базовыми полями из таблицы film
    public Optional<Film> getFilmByIdLite(Long filmId) {
        String sqlQuery = "SELECT film_id, film_name, description, release_date, duration, mpa_rating_id " +
                "FROM film " +
                "WHERE film_id = :filmId";
        SqlParameterSource namedParam = new MapSqlParameterSource("filmId", filmId);

        Optional<Film> filmOptional;
        try {
            filmOptional = Optional.ofNullable(
                    jdbcTemplate.queryForObject(sqlQuery, namedParam, this::mapRowToFilm)
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
