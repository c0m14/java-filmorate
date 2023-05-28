package ru.yandex.practicum.filmorate.repository.film.h2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorNotExistsException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.film.DirectorDao;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class DirectorDaoImpl implements DirectorDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Director findById(Integer id) {
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet("select director_id, director_name from directors where director_id = ?", id);
        if (directorRows.next()) {
            Director director = Director.builder()
                    .id(directorRows.getInt("director_id"))
                    .name(directorRows.getString("director_name").trim())
                    .build();

            log.info("Director found: {} {}", director.getId(), director.getName());
            return director;
        } else {
            log.info("Director with id {} not found.", id);
            throw new DirectorNotExistsException(String.format("Director with id %d not found.", id));
        }
    }

    @Override
    public List<Director> findAll() {
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet("select director_id, director_name from directors");
        List<Director> directors = new ArrayList<>();

        while (directorRows.next()) {
            Director director = Director.builder()
                    .id(directorRows.getInt("director_id"))
                    .name(directorRows.getString("director_name").trim())
                    .build();
            directors.add(director);
        }
        log.info("Got the list of {} directors", directors.size());
        return directors;
    }

    @Override
    public Director add(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("director_id");
        director.setId((int) simpleJdbcInsert.executeAndReturnKey(director.toMap()).longValue());
        log.info("Director created: {} {}.", director.getId(), director.getName());
        return director;
    }

    @Override
    public Director update(Director director) {
        checkDirectorById(director.getId());
        String sqlQuery = "MERGE INTO directors (director_id, director_name) " +
                "VALUES (?, ?);";
        jdbcTemplate.update(sqlQuery, director.getId(), director.getName());

        log.info("Director updated: {} {}", director.getId(), director.getName());
        return director;
    }

    @Override
    public void remove(Integer directorId) {
        checkDirectorById(directorId);
        String sqlQuery = "DELETE FROM film_directors " +
                "WHERE director_id = ?; " +
                "DELETE FROM directors " +
                "WHERE director_id = ?;";
        jdbcTemplate.update(sqlQuery, directorId, directorId);
        log.info("Director id {} deleted", directorId);
    }

    @Override
    public void addDirectorsToFilm(Long filmId, Set<Integer> directorsIds) {
        String sqlQuery = "MERGE INTO film_directors " +
                "VALUES (?, ?)";
        for (Integer directorId : directorsIds) {
            jdbcTemplate.update(sqlQuery, filmId, directorId);
            log.info("Director id {} added to film id {}.", directorId, filmId);
        }
    }

    @Override
    public void removeDirectorsFromFilm(Long filmId) {
        String sqlQuery = "DELETE FROM film_directors " +
                "WHERE film_id = ?;";
        jdbcTemplate.update(sqlQuery, filmId);
        log.info("Directors removed for film id {}", filmId);
    }

    @Override
    public Map<Long, Set<Director>> getDirectorsForFilms(List<Long> filmsIds) {
        String sqlQuery = "SELECT fd.film_id, d.director_id, d.director_name " +
                "FROM directors AS d " +
                "RIGHT JOIN film_directors AS fd ON d.director_id = fd.director_id " +
                "WHERE d.director_id IN " +
                "(SELECT director_id " +
                "FROM film_directors AS fd " +
                "WHERE film_id IN (:filmIds))";
        SqlParameterSource namedParams = new MapSqlParameterSource("filmIds", filmsIds);
        Map<Long, Set<Director>> filmsWithDirectors = new HashMap<>();

        List<Map<Long, Director>> filmIdWithDirectorList = namedParameterJdbcTemplate.query(sqlQuery, namedParams, ((rs, rowNum) -> Collections.singletonMap(
                rs.getLong("film_id"),
                Director.builder()
                        .id(rs.getInt("director_id"))
                        .name(rs.getString("director_name"))
                        .build())));

        filmIdWithDirectorList.stream()
                .flatMap(map -> map.entrySet().stream())
                .forEach((entry -> {
                    if (filmsWithDirectors.containsKey(entry.getKey())) {
                        filmsWithDirectors.get(entry.getKey()).add(entry.getValue());
                    } else {
                        Set<Director> directors = new HashSet<>();
                        directors.add(entry.getValue());
                        filmsWithDirectors.put(entry.getKey(), directors);
                    }
                }
                ));
        return filmsWithDirectors;
    }

    @Override
    public void checkDirectorById(Integer directorId) {
        String sqlQuery = "SELECT director_id FROM directors WHERE director_id = ?";
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet(sqlQuery, directorId);
        if (directorRows.next()) {
            log.info("Director with id {} have found in DB.", directorId);
            return;
        }
        log.warn("Director with id {} not found.", directorId);
        throw new DirectorNotExistsException(String.format("Director with id %d not found.", directorId));
    }
}

