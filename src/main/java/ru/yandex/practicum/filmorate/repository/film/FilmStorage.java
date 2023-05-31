package ru.yandex.practicum.filmorate.repository.film;

import ru.yandex.practicum.filmorate.model.CataloguedFilm;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Optional<Film> getFilmByIdFull(Long filmId);

    void giveLikeFromUserToFilm(Long filmId, Long userId);

    boolean removeUserLikeFromFilm(Long filmId, Long userId);

    List<Film> getPopularFilms(Integer count, Integer genreId, Integer year);

    Map<Long, Set<Long>> fillInUserLikes();

    List<Film> getFilmsByIds(Set<Long> filmIds);

    List<Film> getCommonFilms(Long userId, Long otherUserId);

    void removeFilmById(Long filmId);

    List<Film> getFilmsByDirector(Integer directorId, String sort);

    List<Film> getFilmsByIdListSortedByPopularity(List<Long> filmIds);

    void initiateFilmCatalogue(Map<Long, CataloguedFilm> filmCatalogue);
}
