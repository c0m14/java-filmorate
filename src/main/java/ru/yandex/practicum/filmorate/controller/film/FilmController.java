package ru.yandex.practicum.filmorate.controller.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotExistException;
import ru.yandex.practicum.filmorate.exception.InvalidFilmFieldsException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/films")
@Validated
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) throws InvalidFilmFieldsException {
        log.debug("Got request to create film {}", film);
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) throws InvalidFilmFieldsException, FilmNotExistException {
        log.debug("Got request to update film {}", film);
        return filmService.updateFilm(film);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Got request to get all films");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(
            @Valid
            @PathVariable("id") @Min(1) Long filmId
    ) {
        log.debug("Got request to get film with id: {}", filmId);
        return filmService.getFilmFromStorageById(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void giveLikeFromUserToFilm(
            @Valid
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId
    ) {
        log.debug("Got request to add like from user with id {} to film with id {}", userId, filmId);
        filmService.giveLikeFromUserToFilm(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeUserLikeFromFilm(
            @Valid
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId
    ) {
        log.debug("Got request to remove like from user with id {} from film with id {}", userId, filmId);
        filmService.removeUserLikeFromFilm(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(value = "count", defaultValue = "10") @Min(1) int count
    ) {
        log.debug("Got request to get {} most popular film(s)", count);
        return filmService.getPopularFilms(count);
    }

}