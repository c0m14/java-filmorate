package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.service.validator.FilmFieldsValidator;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmFieldsValidator filmFieldsValidator;
    private final UserFieldsValidator userFieldsValidator;
    private final UserService userService;

    public Film addFilm(Film film) {
        filmFieldsValidator.checkRequestFilm(film, RequestType.CREATE);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        filmFieldsValidator.checkRequestFilm(film, RequestType.UPDATE);
        return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmFromStorageById(Long filmId) {
        Optional<Film> optionalFilm = filmStorage.getFilmById(filmId);
        if (optionalFilm.isEmpty()) {
            throw new FilmNotExistException(String.format("Film with id %d doesn't exist", filmId));
        }
        return optionalFilm.get();
    }

    public void giveLikeFromUserToFilm(Long filmId, Long userId) {
        userFieldsValidator.checkIfPresentById(userId);
        filmFieldsValidator.checkIfPresentById(filmId);

        filmStorage.getFilmById(filmId)
                .get()
                .getLikedFilmIds()
                .add(userId);
    }

    public void removeUserLikeFromFilm(Long filmId, Long userId) {
        filmFieldsValidator.checkIfPresentById(filmId);
        userFieldsValidator.checkIfPresentById(userId);

        filmStorage.getFilmById(filmId)
                .get()
                .getLikedFilmIds()
                .remove(userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted((film1, film2) -> film2.getLikedFilmIds().size() - film1.getLikedFilmIds().size())
                .limit(count)
                .collect(Collectors.toList());
    }
}
