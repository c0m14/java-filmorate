package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotExistException;
import ru.yandex.practicum.filmorate.exception.InvalidFilmFieldsException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RequestType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private Map<Long, Film> films;
    private final DateTimeFormatter formatter;
    private Long idCounter;

    public InMemoryFilmStorage() {
        films = new HashMap<>();
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        idCounter = 1L;
    }

    @Override
    public void setIdCount(Film film) {
        film.setId(idCounter);
        idCounter++;
    }

    @Override
    public Film addFilm(Film film) {
        checkRequestFilm(film, RequestType.CREATE);
        setIdCount(film);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        checkRequestFilm(film, RequestType.UPDATE);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    private void checkRequestFilm(Film film, RequestType requestType) throws InvalidFilmFieldsException {
        if (requestType.equals(RequestType.UPDATE)) {
            checkIfPresent(film);
        }
        checkFilmId(film.getId(), requestType);
        checkFilmReleaseDate(film.getReleaseDate());
    }

    private void checkIfPresent(Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Film with id {} doesn't exist", film.getId());
            throw new FilmNotExistException(
                    String.format("Film with id %d doesn't exist", film.getId())
            );
        }
    }

    private void checkFilmId(Long id, RequestType requestType) throws InvalidFilmFieldsException {
        if (requestType.equals(RequestType.CREATE)) {
            if (id != null) {
                log.error("\"Id\" shouldn't be sent while creation");
                throw new InvalidFilmFieldsException("\"Id\" shouldn't be sent while creation");
            }
        } else if (requestType.equals(RequestType.UPDATE)) {
            if (id <= 0) {
                log.error("\"Id\" isn't positive: {}", id);
                throw new InvalidFilmFieldsException(
                        String.format("\"Id\" isn't positive: %d", id));
            }
        }
    }

    private void checkFilmReleaseDate(LocalDate releaseDate) {
        if (releaseDate
                .isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("\"Release Date\" must be after 1895-12-28: {}", formatter.format(releaseDate));
            throw new InvalidFilmFieldsException(
                    String.format(
                            "\"Release Date\" must be after 1895-12-28: %s",
                            formatter.format(releaseDate)
                    )
            );
        }
    }
}
