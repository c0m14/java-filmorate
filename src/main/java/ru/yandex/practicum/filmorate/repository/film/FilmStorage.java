package ru.yandex.practicum.filmorate.repository.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Optional<Film> getFilmByIdFull(Long filmId);

    void giveLikeFromUserToFilm(Long filmId, Long userId);

    boolean removeUserLikeFromFilm(Long filmId, Long userId);

    List<Film> getPopularFilms(int count);

    List<Film> getCommonFilms(Long userId, Long otherUserId);

    void removeFilmById(Long filmId);
}

