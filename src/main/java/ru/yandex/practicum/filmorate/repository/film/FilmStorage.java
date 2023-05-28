package ru.yandex.practicum.filmorate.repository.film;

import ru.yandex.practicum.filmorate.model.CataloguedFilm;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;
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

    List<Film> getFilmsByDirector(Integer directorId, String sort);

    List<Film> getFilmsByIdListSortedByPopularity(List<Long> filmIds);

    void initiateFilmCatalogue(Map<Long, CataloguedFilm> filmCatalogue);
}

