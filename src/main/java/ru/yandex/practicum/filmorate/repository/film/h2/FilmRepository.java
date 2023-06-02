package ru.yandex.practicum.filmorate.repository.film.h2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.InvalidFieldsException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.CataloguedFilm;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.repository.film.DirectorDao;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.model.Constants.SORT_BY_LIKES;
import static ru.yandex.practicum.filmorate.model.Constants.SORT_BY_YEAR;

@Component
@Qualifier("H2FilmRepository")
@Slf4j
@RequiredArgsConstructor
public class FilmRepository implements FilmStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RatingMpaDao ratingMpaDao;
    private final FilmGenreDao filmGenreDao;
    private final FilmLikesDao filmLikesDao;
    private final DirectorDao directorDao;

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

        if (!film.getDirectors().isEmpty()) {
            Set<Integer> directorsIdSet = film.getDirectors()
                    .stream()
                    .mapToInt(Director::getId)
                    .boxed()
                    .collect(Collectors.toSet());

            directorDao.addDirectorsToFilm(filmId, directorsIdSet);
        }

        film.setId(filmId);
        fetchAdditionalParamsToFilmsList(Collections.singletonList(film));
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

        directorDao.removeDirectorsFromFilm(film.getId());
        if (!film.getDirectors().isEmpty()) {
            Set<Integer> directorsIdSet = film.getDirectors()
                    .stream()
                    .mapToInt(Director::getId)
                    .boxed()
                    .collect(Collectors.toSet());

            directorDao.addDirectorsToFilm(film.getId(), directorsIdSet);
        }

        fetchAdditionalParamsToFilmsList(Collections.singletonList(film));
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
        filmOptional.ifPresent(film -> fetchAdditionalParamsToFilmsList(Collections.singletonList(film)));

        return filmOptional;
    }

    @Override
    public Map<Long, Set<Long>> fillInUserLikes() {
        String sqlQuery = "SELECT u.USER_ID, uf.FILM_ID " +
                "FROM USERS u " +
                "LEFT JOIN USER_FILM_LIKES uf ON u.USER_ID = uf.USER_ID";

        Map<Long, Set<Long>> usersLikedFilmsIds = new HashMap<>();

        SqlParameterSource namedParams = new MapSqlParameterSource();

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, namedParams);
        while (rowSet.next()) {
            long userId = rowSet.getLong("USER_ID");
            Long filmId = rowSet.getLong("FILM_ID");

            if (filmId != null) {
                Set<Long> filmIds = usersLikedFilmsIds.get(userId);
                if (filmIds == null) {
                    filmIds = new HashSet<>();
                    usersLikedFilmsIds.put(userId, filmIds);
                }
                filmIds.add(filmId);
            }
        }
        return usersLikedFilmsIds;
    }

    @Override
    public List<Film> getFilmsByIds(Set<Long> filmIds) {

        List<Film> films = new ArrayList<>();

        if (filmIds.isEmpty()) {
            log.info("Нет рекомендованных фильмов");
            return films;
        }

        StringJoiner filmIdsString = new StringJoiner(",");
        for (long filmId : filmIds) {
            filmIdsString.add(String.valueOf(filmId));
        }

        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                "f.mpa_rating_id, mr.mpa_rating_name " +
                "FROM film AS f " +
                "LEFT JOIN mpa_rating AS mr ON f.mpa_rating_id = mr.mpa_rating_id " +
                "WHERE f.film_id IN (" + filmIdsString + ")";

        films.addAll(jdbcTemplate.query(sqlQuery, this::mapRowToFilm));
        fetchAdditionalParamsToFilmsList(films);
        return films;
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        String genreQuery = "";
        String yearQuery = "";
        MapSqlParameterSource namedParam = new MapSqlParameterSource("count", count);

        if (genreId != null) {
            genreQuery = "JOIN film_genre AS fg ON (fg.film_id = f.film_id AND fg.genre_id = :genreId) ";
            namedParam.addValue("genreId", genreId);
        }

        if (year != null) {
            yearQuery = "WHERE EXTRACT(YEAR from f.release_date) = " +
                    year +
                    " ";
        }

        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                "f.mpa_rating_id, mr.mpa_rating_name " +
                "FROM film AS f " +
                "LEFT JOIN mpa_rating AS mr ON f.mpa_rating_id = mr.mpa_rating_id " +
                "LEFT JOIN user_film_likes AS likes ON f.film_id = likes.film_id " +
                genreQuery +
                yearQuery +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(likes.user_id) DESC " +
                "LIMIT :count";

        List<Film> films;

        try {
            films = jdbcTemplate.query(sqlQuery, namedParam, this::mapRowToFilm);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }

        fetchAdditionalParamsToFilmsList(films);
        return films;

    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long otherUserId) {
        String sqlSubQuery = "(SELECT film_id " +
                "FROM user_film_likes " +
                "WHERE user_id = :userId " +
                "INTERSECT " +
                "SELECT film_id " +
                "FROM user_film_likes " +
                "WHERE user_id = :otherUserId) ";
        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                "f.mpa_rating_id, mr.mpa_rating_name " +
                "FROM film AS f " +
                "LEFT JOIN mpa_rating AS mr ON f.mpa_rating_id = mr.mpa_rating_id " +
                "LEFT JOIN user_film_likes AS likes ON f.film_id = likes.film_id " +
                "WHERE f.film_id IN " + sqlSubQuery +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(likes.user_id) DESC";
        SqlParameterSource namedParam = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("otherUserId", otherUserId);
        List<Film> films;

        try {
            films = jdbcTemplate.query(sqlQuery, namedParam, this::mapRowToFilm);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }

        fetchAdditionalParamsToFilmsList(films);
        return films;
    }

    @Override
    public void removeFilmById(Long filmId) {
        String sqlQuery = "DELETE FROM film " +
                "WHERE film_id = :filmId";

        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("filmId", filmId);

        jdbcTemplate.update(sqlQuery, namedParams);
    }

    public List<Film> getFilmsByDirector(Integer directorId, String sort) {
        directorDao.checkDirectorById(directorId);
        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                "f.mpa_rating_id, mr.mpa_rating_name, d.director_id, d.director_name " +
                "FROM film AS f " +
                "LEFT JOIN mpa_rating AS mr ON f.mpa_rating_id = mr.mpa_rating_id " +
                "LEFT JOIN user_film_likes AS likes ON f.film_id = likes.film_id " +
                "LEFT JOIN film_directors AS fd ON f.film_id = fd.film_id " +
                "LEFT JOIN directors AS d ON fd.director_id = d.director_id " +
                "WHERE d.director_id = :directorId " +
                "GROUP BY f.film_id, mr.mpa_rating_name, d.director_id ";
        if (sort.equals(SORT_BY_YEAR)) {
            sqlQuery = sqlQuery + "ORDER BY f.release_date ASC;";
        } else if (sort.equals(SORT_BY_LIKES)) {
            sqlQuery = sqlQuery + "ORDER BY COUNT(likes.user_id) ASC;";
        } else {
            throw new InvalidFieldsException("Constants", "Wrong sort type in the endpoint");
        }

        SqlParameterSource namedParam = new MapSqlParameterSource()
                .addValue("directorId", directorId);
        List<Film> films;

        try {
            films = jdbcTemplate.query(sqlQuery, namedParam, this::mapRowToFilm);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }

        fetchAdditionalParamsToFilmsList(films);
        return films;
    }

    @Override
    public List<Film> getFilmsByIdListSortedByPopularity(List<Long> filmIds) {
        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                "f.mpa_rating_id, mr.mpa_rating_name " +
                "FROM film AS f " +
                "LEFT JOIN mpa_rating AS mr ON f.mpa_rating_id = mr.mpa_rating_id " +
                "LEFT JOIN user_film_likes AS likes ON f.film_id = likes.film_id " +
                "WHERE f.film_id IN (:filmIds) " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(likes.user_id) DESC";
        SqlParameterSource namedParams = new MapSqlParameterSource("filmIds", filmIds);
        List<Film> films;

        try {
            films = jdbcTemplate.query(sqlQuery, namedParams, this::mapRowToFilm);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }

        fetchAdditionalParamsToFilmsList(films);
        return films;
    }

    @Override
    public void initiateFilmCatalogue(Map<Long, CataloguedFilm> filmCatalogue) {
        String sqlQuery = "SELECT f.film_id, f.film_name, d.director_name " +
                "FROM film f " +
                "LEFT JOIN film_directors fd ON f.film_id = fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id = d.director_id";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, new MapSqlParameterSource());
        while (rowSet.next()) {
            if (!filmCatalogue.containsKey(rowSet.getLong("film_id"))) {
                CataloguedFilm film = new CataloguedFilm(rowSet.getString("film_name").toLowerCase());
                if (rowSet.getString("director_name") != null) {
                    film.addDirector(rowSet.getString("director_name").toLowerCase());
                }
                filmCatalogue.put(rowSet.getLong("film_id"), film);
            } else {
                filmCatalogue.get(rowSet.getLong("film_id"))
                        .addDirector(rowSet.getString("director_name").toLowerCase());
            }
        }

    }

    private void fetchAdditionalParamsToFilmsList(List<Film> films) {
        fetchGenresToFilms(films);
        fetchLikesToFilms(films);
        fetchDirectorsToFilms(films);
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

    private void fetchDirectorsToFilms(List<Film> films) {
        List<Long> filmIds = getIdsFromFilmsList(films);
        Map<Long, Set<Director>> mapFilmIdToDirectors = directorDao.getDirectorsForFilms(filmIds);

        films.forEach(film -> {
            if (mapFilmIdToDirectors.containsKey(film.getId())) {
                film.setDirectors(mapFilmIdToDirectors.get(film.getId()));
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