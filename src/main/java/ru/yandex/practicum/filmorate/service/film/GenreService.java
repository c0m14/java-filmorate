package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotExistsException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.film.h2.FilmGenreDao;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final FilmGenreDao filmGenreDao;

    public Genre getGenreById(int id) {
        if (id <= 0) {
            throw new NotExistsException(
                    "Genre",
                    String.format("Genre with id %d does not exist", id)
            );
        }
        return filmGenreDao.getGenreById(id);
    }

    public List<Genre> getAllGenres() {
        return filmGenreDao.getAllGenres();
    }
}
