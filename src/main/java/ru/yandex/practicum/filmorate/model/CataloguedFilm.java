package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class CataloguedFilm {
    private final String filmName;
    private final Set<String> filmDirectors = new HashSet<>();

    public CataloguedFilm(Film film) {
        filmName = film.getName().toLowerCase();
        film.getDirectors().forEach(director -> filmDirectors.add(director.getName().toLowerCase()));
    }

    public void addDirector(String director) {
        filmDirectors.add(director);
    }
}