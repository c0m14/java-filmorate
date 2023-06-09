package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.NotExistsException;
import ru.yandex.practicum.filmorate.model.CataloguedFilm;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.model.feed.OperationType;
import ru.yandex.practicum.filmorate.repository.feed.FeedStorage;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;
import ru.yandex.practicum.filmorate.repository.film.h2.FilmLikesDao;
import ru.yandex.practicum.filmorate.service.validator.FilmFieldsValidator;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.model.Constants.SEARCH_BY_TITLE;
import static ru.yandex.practicum.filmorate.model.Constants.SEARCH_BY_DIRECTOR;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmFieldsValidator filmFieldsValidator;
    private final UserFieldsValidator userFieldsValidator;
    private final FilmLikesDao filmLikesDao;
    private final FeedStorage feedStorage;
    private final Map<Long, CataloguedFilm> filmCatalogue = new HashMap<>();

    @Autowired
    public FilmService(@Qualifier("H2FilmRepository") FilmStorage filmStorage,
                       FilmFieldsValidator filmFieldsValidator,
                       UserFieldsValidator userFieldsValidator,
                       FilmLikesDao filmLikesDao,
                       FeedStorage feedStorage) {
        this.filmStorage = filmStorage;
        this.filmFieldsValidator = filmFieldsValidator;
        this.userFieldsValidator = userFieldsValidator;
        this.filmLikesDao = filmLikesDao;
        this.feedStorage = feedStorage;
        initiateFilmCatalogue();
    }

    public Film addFilm(Film film) {
        filmFieldsValidator.checkRequestFilm(film, RequestType.CREATE);
        film = filmStorage.addFilm(film);
        filmCatalogue.put(film.getId(), new CataloguedFilm(film));
        return film;
    }

    public Film updateFilm(Film film) {
        filmFieldsValidator.checkRequestFilm(film, RequestType.UPDATE);
        Film result = filmStorage.updateFilm(film);
        filmCatalogue.put(result.getId(), new CataloguedFilm(result));
        return result;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmFromStorageById(Long filmId) {
        return filmStorage.getFilmByIdFull(filmId).orElseThrow(
                () -> new NotExistsException(
                "Film",
                String.format("Film with id %d does not exist", filmId)
        ));
    }

    public void giveLikeFromUserToFilm(Long filmId, Long userId) {
        userFieldsValidator.checkIfPresentById(userId);
        filmFieldsValidator.checkIfPresentById(filmId);

        filmLikesDao.setFilmLike(filmId, userId);
        Feed feed = Feed.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(OperationType.ADD)
                .entityId(filmId)
                .build();
        feedStorage.addEvent(feed);
    }

    public void removeUserLikeFromFilm(Long filmId, Long userId) {
        filmFieldsValidator.checkIfPresentById(filmId);
        userFieldsValidator.checkIfPresentById(userId);

        Feed feed = Feed.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(OperationType.REMOVE)
                .entityId(filmId)
                .build();
        feedStorage.addEvent(feed);
        filmLikesDao.removeFilmLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {

        List<Film> films;
        if (genreId != null) {
            filmFieldsValidator.checkIfGenrePresentById(genreId);
        }
        films = filmStorage.getPopularFilms(count, genreId, year);
        return films;
    }

    public List<Film> getCommonFilms(Long userId, Long otherUserId) {
        userFieldsValidator.checkIfPresentById(userId);
        userFieldsValidator.checkIfPresentById(otherUserId);

        return filmStorage.getCommonFilms(userId, otherUserId);
    }

    public void removeFilmById(Long filmId) {
        filmFieldsValidator.checkIfPresentById(filmId);

        filmStorage.removeFilmById(filmId);
        filmCatalogue.remove(filmId);
    }

    public List<Film> getFilmsByDirector(Integer directorId, String sort) {
        return filmStorage.getFilmsByDirector(directorId, sort);
    }

    public List<Film> searchFilms(String query, List<String> by) {
        return filmStorage.getFilmsByIdListSortedByPopularity(
                getFilmIdListBySearchInCatalogue(query.toLowerCase(), validateAndSetParameterByForSearch(by))
        );
    }

    public List<String> validateAndSetParameterByForSearch(List<String> by) {
        List<String> result;
        if (by == null || by.isEmpty()) {
            result = List.of(SEARCH_BY_TITLE, SEARCH_BY_DIRECTOR);
        } else if (by.contains("title")) {
            if (by.contains("director")) {
                result = List.of(SEARCH_BY_TITLE, SEARCH_BY_DIRECTOR);
            } else {
                result = List.of(SEARCH_BY_TITLE);
            }
        } else if (by.contains("director")) {
            result = List.of(SEARCH_BY_DIRECTOR);
        } else {
            throw new IncorrectParameterException("By", "Should be title, director or both");
        }
        return result;
    }

    private List<Long> getFilmIdListBySearchInCatalogue(String query, List<String> by) {
        return filmCatalogue.entrySet().stream()
                .filter(entry -> searchFilter(entry.getValue(), query, by))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private boolean searchFilter(CataloguedFilm cataloguedFilm, String query, List<String> by) {
        boolean result = false;
        if (by.contains(SEARCH_BY_TITLE)) {
            if (cataloguedFilm.getFilmName().contains(query)) {
                result = true;
            }
        }
        if (by.contains(SEARCH_BY_DIRECTOR)) {
            if (cataloguedFilm.getFilmDirectors().stream().anyMatch(director -> director.contains(query))) {
                result = true;
            }
        }
        return result;
    }

    private void initiateFilmCatalogue() {
        filmStorage.initiateFilmCatalogue(filmCatalogue);
    }
}