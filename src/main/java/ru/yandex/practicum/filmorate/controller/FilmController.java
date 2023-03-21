package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotExistException;
import ru.yandex.practicum.filmorate.exception.InvalidFilmFieldsException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/films")
public class FilmController {

    private Map<Integer, Film> films = new HashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private int idCounter = 1;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) throws InvalidFilmFieldsException {
        log.debug("Got request to create film {}", film);
        checkRequestFilm(film, RequestType.CREATE);
        film.setId(idCounter++);
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) throws InvalidFilmFieldsException, FilmNotExistException {
        log.debug("Got request to update film {}", film);
        checkRequestFilm(film, RequestType.UPDATE);
        if (!films.containsKey(film.getId())) {
            log.error("Film with id {} doesn't exist", film.getId());
            throw new FilmNotExistException(
                    String.format("Film with id %d doesn't exist", film.getId())
            );
        }
        films.put(film.getId(), film);
        return film;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    private void checkRequestFilm(Film film, RequestType requestType) throws InvalidFilmFieldsException {
        checkFilmId(film.getId(), requestType);
        checkFilmReleaseDate(film.getReleaseDate());
    }

    private void checkFilmId(Integer id, RequestType requestType) throws InvalidFilmFieldsException {
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
