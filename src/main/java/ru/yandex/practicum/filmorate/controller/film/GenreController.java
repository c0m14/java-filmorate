package ru.yandex.practicum.filmorate.controller.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.film.GenreService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/genres")
public class GenreController {
    private final GenreService genreService;

    @GetMapping("/{id}")
    public Genre getGenreById(
            @PathVariable("id") int id) {
        log.debug("Got request to get genre with id: {}", id);
        return genreService.getGenreById(id);
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        log.debug("Got request to get all genres");
        return genreService.getAllGenres();
    }
}
