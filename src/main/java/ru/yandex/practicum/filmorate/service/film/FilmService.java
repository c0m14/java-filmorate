package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotExistException;
import ru.yandex.practicum.filmorate.model.CataloguedFilm;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;
import ru.yandex.practicum.filmorate.service.validator.FilmFieldsValidator;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmFieldsValidator filmFieldsValidator;
    private final UserFieldsValidator userFieldsValidator;
    private final Map<Long, CataloguedFilm> filmCatalogue = new HashMap<>();

    @Autowired
    public FilmService(@Qualifier("H2FilmRepository") FilmStorage filmStorage,
            FilmFieldsValidator filmFieldsValidator,
            UserFieldsValidator userFieldsValidator) {
        this.filmStorage = filmStorage;
        this.filmFieldsValidator = filmFieldsValidator;
        this.userFieldsValidator = userFieldsValidator;
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
                () -> new FilmNotExistException(String.format("Film with id %d doesn't exist", filmId))
        );
    }

    public void giveLikeFromUserToFilm(Long filmId, Long userId) {
        userFieldsValidator.checkIfPresentById(userId);
        filmFieldsValidator.checkIfPresentById(filmId);

        filmStorage.giveLikeFromUserToFilm(filmId, userId);
    }

    public void removeUserLikeFromFilm(Long filmId, Long userId) {
        filmFieldsValidator.checkIfPresentById(filmId);
        userFieldsValidator.checkIfPresentById(userId);

        filmStorage.removeUserLikeFromFilm(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public List<Film> getCommonFilms(Long userId, Long otherUserId) {
        userFieldsValidator.checkIfPresentById(userId);
        userFieldsValidator.checkIfPresentById(otherUserId);

        return filmStorage.getCommonFilms(userId, otherUserId);
    }

    public List<Film> getFilmsByDirector(Integer directorId, String sort) {
        return filmStorage.getFilmsByDirector(directorId, sort);
    }

    public List<Film> searchFilms(String query, List<String> by) {
        return filmStorage.getFilmsByIdListSortedByPopularity(
                getFilmIdListBySearchInCatalogue(query, by)
        );
    }

    private List<Long> getFilmIdListBySearchInCatalogue(String query, List<String> by) {
        return filmCatalogue.entrySet().stream()
                .filter(entry -> searchFilter(entry.getValue(), query, by))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private boolean searchFilter(CataloguedFilm cataloguedFilm, String query, List<String> by) {
        boolean result = false;
        if (by.contains("title")) {
            if (cataloguedFilm.getFilmName().contains(query)) {
                result = true;
            }
        }
        if (by.contains("director")) {
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
