package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.service.validator.FilmFieldsValidator;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

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

    // TODO
    public void giveLikeFromUserToFilm(Long filmId, Long userId) {
        userFieldsValidator.checkIfPresentById(userId);
        filmFieldsValidator.checkIfPresentById(filmId);

        filmStorage.getFilmByIdFull(filmId)
                .get()
                .getLikesCount();
                //.add(userId);
    }

    //TODO
    public void removeUserLikeFromFilm(Long filmId, Long userId) {
        filmFieldsValidator.checkIfPresentById(filmId);
        userFieldsValidator.checkIfPresentById(userId);

        filmStorage.getFilmByIdFull(filmId)
                .get()
                .getLikesCount();
                //.remove(userId);
    }

    //TODO
    public List<Film> getPopularFilms(int count) {
        return  null; /*filmStorage.getAllFilms().stream()
                .sorted((film1, film2) -> film2.getLikesCount().size() - film1.getLikesCount().size())
                .limit(count)
                .collect(Collectors.toList());*/
    }
}
