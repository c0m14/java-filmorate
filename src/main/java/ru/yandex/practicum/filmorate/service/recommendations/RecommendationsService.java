package ru.yandex.practicum.filmorate.service.recommendations;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecommendationsService {

    private final FilmStorage filmStorage;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RecommendationsService(@Qualifier("H2FilmRepository") FilmStorage filmStorage, JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Film> getRecommendations(long userId) {
        Set<Long> similarUsers = findUsersWithIntersectingLikes(userId);
        Set<Long> recommendedFilmIds = findRecommendedFilmIds(userId, similarUsers);
        return filmStorage.getFilmsByIds(recommendedFilmIds);
    }

    public Set<Long> findUsersWithIntersectingLikes(long userId) {
        String sqlQuery = "SELECT DISTINCT uf2.USER_ID " +
                "FROM USER_FILM_LIKES uf " +
                "JOIN USER_FILM_LIKES uf2 ON uf.FILM_ID = uf2.FILM_ID " +
                "WHERE uf.USER_ID = ? AND uf2.USER_ID != ?";

        Object[] params = {userId, userId};

        List<Integer> results = jdbcTemplate.queryForList(sqlQuery, params, Integer.class);

        Set<Long> similarUserIds = new HashSet<>();
        for (int userIdObj : results) {
            similarUserIds.add((long) userIdObj);
        }

        return similarUserIds;
    }

    public Set<Long> findRecommendedFilmIds(long userId, Set<Long> similarUsers) {
        String sqlQuery = "SELECT DISTINCT uf.FILM_ID " +
                "FROM USER_FILM_LIKES uf " +
                "LEFT JOIN USER_FILM_LIKES uf2 ON uf.FILM_ID = uf2.FILM_ID AND uf2.USER_ID = ? " +
                "WHERE uf.USER_ID IN (?) AND uf2.USER_ID IS NULL";

        Object[] params = {userId, similarUsers.toArray()};

        List<Integer> results = jdbcTemplate.queryForList(sqlQuery, params, Integer.class);

        Set<Long> recommendedFilmIds = new HashSet<>();
        for (int filmIdObj : results) {
            recommendedFilmIds.add((long) filmIdObj);
        }

        return recommendedFilmIds;
    }
}