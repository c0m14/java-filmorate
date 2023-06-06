package ru.yandex.practicum.filmorate.repository.film;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DirectorDao {

    Director findById(Integer directorId);

    List<Director> findAll();

    Director add(Director director);

    Director update(Director director);

    void remove(Integer directorId);

    void addDirectorsToFilm(Long filmId, Set<Integer> directorsIds);

    void removeDirectorsFromFilm(Long filmId);

    Map<Long, Set<Director>> getDirectorsForFilms(List<Long> filmsIds);

    void checkDirectorById(Integer directorId);
}
