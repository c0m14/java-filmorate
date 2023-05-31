package ru.yandex.practicum.filmorate.service.recommendations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;

import java.util.*;

@Service
@Slf4j
public class RecommendationsService {

    private final FilmStorage filmStorage;

    public RecommendationsService(@Qualifier("H2FilmRepository") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> getRecommendations(long userId) {
        Map<Long, Set<Long>> usersLikedFilmsIds = filmStorage.fillInUserLikes();
        Set<Long> similarUsers = findUsersWithIntersectingLikes(userId, usersLikedFilmsIds);
        Set<Long> recommendedFilmIds = findRecommendedFilmIds(userId, similarUsers, usersLikedFilmsIds);
        return filmStorage.getFilmsByIds(recommendedFilmIds);
    }

    public Set<Long> findUsersWithIntersectingLikes(long userId, Map<Long, Set<Long>> usersLikedFilmsIds) {
        Set<Long> similarUsers = new HashSet<>();
        Set<Long> userLikedFilms = usersLikedFilmsIds.get(userId);

        for (Map.Entry<Long, Set<Long>> entry : usersLikedFilmsIds.entrySet()) {
            long otherUserId = entry.getKey();

            if (otherUserId != userId) {
                Set<Long> otherUserLikedFilms = entry.getValue();

                boolean intersection = false;
                for (long filmId : userLikedFilms) {
                    if (otherUserLikedFilms.contains(filmId)) {
                        intersection = true;
                        break;
                    }
                }

                if (intersection) {
                    similarUsers.add(otherUserId);
                }
            }
        }

        return similarUsers;
    }

    public Set<Long> findRecommendedFilmIds(long userId, Set<Long> similarUsers, Map<Long, Set<Long>> usersLikedFilmsIds) {
        Set<Long> recommendedFilmIds = new HashSet<>();
        Set<Long> userLikedFilms = usersLikedFilmsIds.get(userId);

        for (long similarUserId : similarUsers) {
            Set<Long> similarUserLikedFilms = usersLikedFilmsIds.get(similarUserId);

            for (long filmId : similarUserLikedFilms) {
                if (!userLikedFilms.contains(filmId)) {
                    recommendedFilmIds.add(filmId);
                }
            }
        }

        return recommendedFilmIds;
    }
}