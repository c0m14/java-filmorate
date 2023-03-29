package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    void setIdCount(Film film);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();
}
