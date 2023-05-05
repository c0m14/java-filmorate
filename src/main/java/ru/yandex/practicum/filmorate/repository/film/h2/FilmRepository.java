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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
            Set<Integer> genresIdSet = film.getGenres()
                    .stream()
                    .mapToInt(Genre::getId)
                    .boxed()
                    .collect(Collectors.toSet());

            filmGenreDao.setGenresToFilm(filmId, genresIdSet);
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

        filmGenreDao.clearGenresFromFilm(film.getId());
        if (!film.getGenres().isEmpty()) {
            Set<Integer> genresIdSet = film.getGenres()
                    .stream()
                    .mapToInt(Genre::getId)
                    .boxed()
                    .collect(Collectors.toSet());

            filmGenreDao.setGenresToFilm(film.getId(), genresIdSet);
        }

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                "f.mpa_rating_id, mr.mpa_rating_name " +
                "FROM film AS f " +
                "LEFT JOIN mpa_rating AS mr ON f.mpa_rating_id = mr.mpa_rating_id";

        List<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
        fetchAdditionalParamsToFilmsList(films);
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
                "f.mpa_rating_id, mr.mpa_rating_name " +
                "FROM film AS f " +
                "LEFT JOIN mpa_rating AS mr ON f.mpa_rating_id = mr.mpa_rating_id " +
                "LEFT JOIN user_film_likes AS likes ON f.film_id = likes.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY SUM(likes.user_id) DESC " +
                "LIMIT :count";
        SqlParameterSource namedParam = new MapSqlParameterSource("count", count);
        List<Film> films;

        try {
            films = jdbcTemplate.query(sqlQuery, namedParam, this::mapRowToFilm);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }

        fetchAdditionalParamsToFilmsList(films);
        return films;

    }

    private void fetchAdditionalParams(Film film) {
        fetchGenres(film);
        fetchLikes(film);
    }

    private void fetchAdditionalParamsToFilmsList(List<Film> films) {
        fetchGenresToFilms(films);
        fetchLikesToFilms(films);
    }

    private void fetchGenres(Film film) {
        film.setGenres(filmGenreDao.getGenresToFilm(film.getId()));
    }

    private void fetchGenresToFilms(List<Film> films) {
        List<Long> filmIds = getIdsFromFilmsList(films);
        Map<Long, Set<Genre>> mapFilmIdToGenres = filmGenreDao.getGenresToFilms(filmIds);

        films.forEach(film -> {
            if (mapFilmIdToGenres.containsKey(film.getId())) {
                film.setGenres(mapFilmIdToGenres.get(film.getId()));
            }
        });
    }

    private void fetchLikes(Film film) {
        film.setLikesCount(filmLikesDao.getFilmLikes(film.getId()));
    }

    private void fetchLikesToFilms(List<Film> films) {
        List<Long> filmsIds = getIdsFromFilmsList(films);
        Map<Long, Long> filmsIdsMapToCountLikes = filmLikesDao.getFilmsLikes(filmsIds);

        if (filmsIdsMapToCountLikes.isEmpty()) {
            return;
        }

        films.forEach(film -> {
            if (filmsIdsMapToCountLikes.containsKey(film.getId())) {
                film.setLikesCount(filmsIdsMapToCountLikes.get(film.getId()));
            }
        });

    }

    private List<Long> getIdsFromFilmsList(List<Film> films) {
        return films.stream()
                .mapToLong(Film::getId)
                .boxed()
                .collect(Collectors.toList());
    }

    //Возвращает фильм с базовыми полями из таблицы film
    public Optional<Film> getFilmByIdLite(Long filmId) {
        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                "f.mpa_rating_id, mr.mpa_rating_name " +
                "FROM film f " +
                "LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.mpa_rating_id " +
                "WHERE f.film_id = :filmId";
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
                        .name(resultSet.getString("mpa_rating_name"))
                        .build()
                )
                .build();
    }
}
