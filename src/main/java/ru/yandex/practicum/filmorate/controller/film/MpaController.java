package ru.yandex.practicum.filmorate.controller.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(value = "/mpa")
public class MpaController {

    private final FilmService filmService;

    @GetMapping("/{id}")
    public RatingMPA getMpaById (
            @Valid
            @PathVariable("id")@Min(1) int id) {
        log.debug("Got request to get rating mpa with id {}", id);
        return filmService.getMpaById(id);
    }

    @GetMapping
    public List<RatingMPA> getAllMpa(){
        log.debug("Got request to get all ratings mpa");
        return filmService.getAllMpa();
    }
}
