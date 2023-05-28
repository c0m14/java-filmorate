package ru.yandex.practicum.filmorate.controller.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.InvalidFieldsException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.film.DirectorService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/directors")
@Validated
public class DirectorController {
    @Autowired
    DirectorService directorService;

    @GetMapping //GET /directors - Список всех режиссёров
    public List<Director> findAll() {
        log.debug("Got request to get all directors");
        return directorService.findAll();
    }

    @GetMapping("/{id}") //GET /directors/{id}- Получение режиссёра по id
    public Director findById(
            @Valid
            @PathVariable("id") @Min(1) Integer directorId
    ) {
        log.debug("Got request to get director with id: {}", directorId);
        return directorService.findById(directorId);
    }

    @PostMapping //POST /directors - Создание режиссёра
    public Director add(@Valid @RequestBody Director director) throws InvalidFieldsException {
        log.debug("Got request to create director: {}", director);
        return directorService.add(director);
    }

    @PutMapping //PUT /directors - Изменение режиссёра
    public Director update(@Valid @RequestBody Director director) throws InvalidFieldsException {
        log.debug("Got request to update film {}", director);
        return directorService.update(director);
    }

    @DeleteMapping("/{id}") //DELETE /directors/{id} - Удаление режиссёра
    public void remove(
            @Valid
            @PathVariable("id") @Min(1) Integer directorId
    ) {
        log.debug("Got request to remove director with id {}", directorId);
        directorService.remove(directorId);
    }
}
