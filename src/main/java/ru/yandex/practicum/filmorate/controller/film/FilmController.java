package ru.yandex.practicum.filmorate.controller.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

import static ru.yandex.practicum.filmorate.model.Constants.SORTS;
import static ru.yandex.practicum.filmorate.model.Constants.SORT_BY_YEAR;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/films")
@Validated
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.debug("Got request to create film {}", film);
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
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
            @PathVariable("id") @Min(1) Long filmId
    ) {
        log.debug("Got request to get film with id: {}", filmId);
        return filmService.getFilmFromStorageById(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void giveLikeFromUserToFilm(
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId
    ) {
        log.debug("Got request to add like from user with id {} to film with id {}", userId, filmId);
        filmService.giveLikeFromUserToFilm(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeUserLikeFromFilm(
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId
    ) {
        log.debug("Got request to remove like from user with id {} from film with id {}", userId, filmId);
        filmService.removeUserLikeFromFilm(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(value = "count", defaultValue = "10") @Min(1) int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year
    ) {
        log.debug("Got request to get {} most popular film(s)", count);
        if (genreId != null) {
            log.debug("With genreId {}", genreId);
        }
        if (year != null) {
            log.debug("With year {}", year);
        }
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(
            @RequestParam(value = "userId") @Min(1) Long userId,
            @RequestParam(value = "friendId") @Min(1) Long friendId
    ) {
        log.debug("Got request to find common films to users with id {} and {}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping("/{filmId}")
    public void removeFilmById(
            @PathVariable("filmId") Long filmId
    ) {
        log.debug("Got request to remove film with id {}", filmId);
        filmService.removeFilmById(filmId);
    }

    @GetMapping("/director/{directorId}") // GET /films/director/{directorId}?sortBy=[year,likes]
    public List<Film> getFilmsByDirector(
            @PathVariable("directorId") @Min(1) Integer directorId,
            @RequestParam(defaultValue = SORT_BY_YEAR, required = false) String sortBy
    ) {
        if (!SORTS.contains(sortBy)) {
            throw new IncorrectParameterException("sortBy", "Should be year or likes");
        }
        log.debug("Got request to get films by director: {}", directorId);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam(value = "query") @NotBlank String query,
            @RequestParam(value = "by", required = false) List<String> by
    ) {
        StringBuilder stringBuilder = new StringBuilder("Got request to search films");
        if (by != null && !by.isEmpty()) {
            stringBuilder.append(" by");
            by.forEach(value -> stringBuilder.append(" ").append(value).append(" or"));
            stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length());
        }
        stringBuilder.append(" with substring ").append(query);
        log.debug(stringBuilder.toString());

        by = filmService.validateAndSetParameterByForSearch(by);

        return filmService.searchFilms(query.toLowerCase(), by);
    }
}