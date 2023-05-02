package ru.yandex.practicum.filmorate.repository.film.inMemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;

import java.util.*;

@Slf4j
@Component("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private Long idCounter = 1L;

    private void setIdCount(Film film) {
        film.setId(idCounter);
        idCounter++;
    }

    @Override
    public Film addFilm(Film film) {
        setIdCount(film);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> getFilmByIdFull(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public void giveLikeFromUserToFilm(Long filmId, Long userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeUserLikeFromFilm(Long filmId, Long userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RatingMPA getMpaById(int mapId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<RatingMPA> getAllMpa() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Genre getGenreById(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Genre> getAllGenres() {
        throw new UnsupportedOperationException();
    }
}