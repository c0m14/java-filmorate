package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;
import ru.yandex.practicum.filmorate.service.validator.FilmFieldsValidator;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("H2FilmRepository")
    private final FilmStorage filmStorage;

    private final FilmFieldsValidator filmFieldsValidator;
    private final UserFieldsValidator userFieldsValidator;

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

}
