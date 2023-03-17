package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.FilmNotExistException;
import ru.yandex.practicum.filmorate.exceptions.InvalidFilmFieldsException;
import ru.yandex.practicum.filmorate.model.Film;

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
    public Film createFilm(@RequestBody Film film) throws InvalidFilmFieldsException {
        log.debug("Got request to create film {}", film);
        checkRequestFilm(film, RequestType.CREATE);
        film.setId(idCounter++);
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) throws InvalidFilmFieldsException, FilmNotExistException {
        log.debug("Got request to update film {}", film);
        checkRequestFilm(film, RequestType.UPDATE);
        if (!films.keySet().contains(film.getId())) {
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
        checkFilmName(film.getName());
        checkFilmDescription(film.getDescription());
        checkFilmReleaseDate(film.getReleaseDate());
        checkFilmDuration(film.getDuration());
    }

    private void checkFilmId(int id, RequestType requestType) throws InvalidFilmFieldsException {
        if (requestType.equals(RequestType.CREATE)) {
            if (id != 0) {
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

    private void checkFilmName(String name) throws InvalidFilmFieldsException {
        if (name == null || name.isBlank()) {
            log.error("\"Name\" is empty");
            throw new InvalidFilmFieldsException("\"Name\" is empty");
        }
    }

    private void checkFilmDescription(String description) throws InvalidFilmFieldsException {
        if (description == null) {
            log.error("\"Description\" is absent in request");
        }
        if (description.length() > 200) {
            log.error("\"Description\" length must be less then 200 characters: {}", description.length());
            throw new InvalidFilmFieldsException(
                    String.format(
                            "\"Description\" length must be less then 200 characters: %d",
                            description.length()
                    )
            );
        }
    }

    private void checkFilmReleaseDate(LocalDate releaseDate) {
        if (releaseDate == null) {
            log.error("\"Release Date\" is absent in request");
            throw new InvalidFilmFieldsException("\"Release Date\" is absent in request");
        }
        if (releaseDate
                .isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("\"Release Date\" must be after 28-12-1895: {}", formatter.format(releaseDate));
            throw new InvalidFilmFieldsException(
                    String.format(
                            "\"Release Date\" must be after 28-12-1895: %s",
                            formatter.format(releaseDate)
                    )
            );
        }
    }

    private void checkFilmDuration(int duration) throws InvalidFilmFieldsException {
        if (duration <= 0) {
            log.error("\"Duration\" must be positive: {}", duration);
            throw new InvalidFilmFieldsException(
                    String.format(
                            "\"Duration\" must be positive: %s",
                            duration
                    )
            );
        }
    }
}
