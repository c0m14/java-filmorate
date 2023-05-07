package ru.yandex.practicum.filmorate.controller.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.film.GenreService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/genres")
public class GenreController {
    private final GenreService genreService;

    @GetMapping("/{id}")
    public Genre getGenreById(
            @Valid
            @PathVariable("id") @Min(1) int id) {
        log.debug("Got request to get genre with id: {}", id);
        return genreService.getGenreById(id);
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        log.debug("Got request to get all genres");
        return genreService.getAllGenres();
    }
}
