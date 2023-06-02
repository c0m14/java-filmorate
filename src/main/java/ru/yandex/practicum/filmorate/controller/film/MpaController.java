package ru.yandex.practicum.filmorate.controller.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.service.film.RatingMpaService;

import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/mpa")
public class MpaController {
    private final RatingMpaService ratingMpaService;

    @GetMapping("/{id}")
    public RatingMPA getMpaById(
            @PathVariable("id") @Min(1) int id) {
        log.debug("Got request to get rating mpa with id {}", id);
        return ratingMpaService.getMpaById(id);
    }

    @GetMapping
    public List<RatingMPA> getAllMpa() {
        log.debug("Got request to get all ratings mpa");
        return ratingMpaService.getAllMpa();
    }
}
