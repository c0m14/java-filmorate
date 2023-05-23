package ru.yandex.practicum.filmorate.service.recommendations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class RecommendationsService {

    private Map<Long, Set<Long>> usersLikedFilmsIds;
    private final JdbcTemplate jdbcTemplate;

    public RecommendationsService(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    private Set<Long> findUsersWithIntersectingLikes(long userId) {
        Set<Long> similarUsers = new HashSet<>();
        Set<Long> userLikedFilms = usersLikedFilmsIds.get(userId);

        for (Map.Entry<Long, Set<Long>> entry : usersLikedFilmsIds.entrySet()) {
            long otherUserId = entry.getKey();

            if (otherUserId == userId || entry.getValue().isEmpty()) {
                continue;
            }

            Set<Long> otherUserLikedFilms = entry.getValue();

            boolean hasDifferentLikedFilms = false;
            for (long filmId : userLikedFilms) {
                if (!otherUserLikedFilms.contains(filmId)) {
                    hasDifferentLikedFilms = true;
                    break;
                }
            }

            if (hasDifferentLikedFilms) {
                similarUsers.add(otherUserId);
            }
        }

        return similarUsers;
    }

    private Map<Long, Set<Long>> fillInUserLikes() {
        String sqlQuery = "SELECT uf.USER_ID, uf.FILM_ID " +
                "FROM USERS u " +
                "INNER JOIN USER_FILM_LIKES uf ON u.USER_ID = uf.USER_ID";

        Map<Long, Set<Long>> usersLikedFilmsIds = new HashMap<>();

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery);
        while (rowSet.next()) {
            long userId = rowSet.getLong("USER_ID");
            long filmId = rowSet.getLong("FILM_ID");

            Set<Long> filmsIds = usersLikedFilmsIds.computeIfAbsent(userId, k -> new HashSet<>());
            filmsIds.add(filmId);
        }

        return usersLikedFilmsIds;
    }

    private Set<Long> findRecommendedFilmIds(long userId, Set<Long> similarUsers) {
        Set<Long> recommendedFilmIds = new HashSet<>();

        for (long similarUserId : similarUsers) {
            Set<Long> similarUserLikedFilms = usersLikedFilmsIds.get(similarUserId);

            for (long filmId : similarUserLikedFilms) {
                if (!usersLikedFilmsIds.get(userId).contains(filmId)) {
                    recommendedFilmIds.add(filmId);
                }
            }
        }
        return recommendedFilmIds;
    }

    private List<Film> getFilmsByIds(Set<Long> filmIds) {
        List<Film> films = new ArrayList<>();

        if (filmIds.isEmpty()) {
            log.info("Нет рекомендованных фильмов");
            return films;
        }

        StringJoiner filmIdsString = new StringJoiner(",");
        for (long filmId : filmIds) {
            filmIdsString.add(String.valueOf(filmId));
        }

        String sqlQuery = "SELECT f.*, g.ID, g.NAME AS GENRE_NAME " +
                "FROM FILMS f " +
                "JOIN FILM_GENRES fg ON f.FILM_ID = fg.FILM_ID " +
                "JOIN GENRES g ON fg.GENRE_ID = g.GENRE_ID " +
                "WHERE f.FILM_ID IN (" + filmIdsString + ")";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery);

        Map<Long, Film> filmMap = new HashMap<>();

        while (rowSet.next()) {
            long id = rowSet.getLong("FILM_ID");
            String name = rowSet.getString("NAME");
            String description = rowSet.getString("DESCRIPTION");
            LocalDate releaseDate = rowSet.getDate("RELEASE_DATE").toLocalDate();
            int duration = rowSet.getInt("DURATION");
            int genreId = rowSet.getInt("ID");
            String genreName = rowSet.getString("GENRE_NAME");

            Film film = filmMap.get(id);
            if (film == null) {
                film = new Film(id, name, description, releaseDate, duration);
                filmMap.put(id, film);
            }

            Genre genre = new Genre(genreId, genreName);
            film.getGenres().add(genre);
        }

        films.addAll(filmMap.values());

        return films;
    }

    public List<Film> getRecommendations(long userId) {
        usersLikedFilmsIds = fillInUserLikes();
        Set<Long> similarUsers = findUsersWithIntersectingLikes(userId);
        Set<Long> recommendedFilmIds = findRecommendedFilmIds(userId, similarUsers);
        return getFilmsByIds(recommendedFilmIds);
    }
}