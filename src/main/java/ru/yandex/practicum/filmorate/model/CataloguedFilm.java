package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class CataloguedFilm {
    private final String filmName;
    private final Set<String> filmDirectors = new HashSet<>();

    public CataloguedFilm(Film film) {
        filmName = film.getName().toLowerCase();
        filmDirectors.addAll(film.getDirectors()
                .stream()
                .map(director -> director.getName().toLowerCase())
                .collect(Collectors.toList()));
    }

    public void addDirector(String director) {
        filmDirectors.add(director);
    }
}