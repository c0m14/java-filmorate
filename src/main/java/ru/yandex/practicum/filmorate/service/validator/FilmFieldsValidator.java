package ru.yandex.practicum.filmorate.service.validator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotExistException;
import ru.yandex.practicum.filmorate.exception.InvalidFilmFieldsException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;
import ru.yandex.practicum.filmorate.repository.film.h2.FilmGenreDao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FilmFieldsValidator {

    @Qualifier("H2FilmRepository")
    FilmStorage filmStorage;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    FilmGenreDao filmGenreStorage;

    public void checkRequestFilm(Film film, RequestType requestType) throws InvalidFilmFieldsException {
        checkFilmId(film.getId(), requestType);
        if (requestType.equals(RequestType.UPDATE)) {
            checkIfPresent(film);
        }
        checkFilmReleaseDate(film.getReleaseDate());
    }

    public void checkIfPresent(Film film) {
        if (filmStorage.getFilmByIdFull(film.getId()).isEmpty()) {
            throw new FilmNotExistException(
                    String.format("Film with id %d doesn't exist", film.getId())
            );
        }
    }

    public void checkIfPresentById(Long filmId) {
        if (filmStorage.getFilmByIdFull(filmId).isEmpty()) {
            throw new FilmNotExistException(
                    String.format("Film with id %d doesn't exist", filmId)
            );
        }
    }

    private void checkFilmId(Long id, RequestType requestType) throws InvalidFilmFieldsException {
        if (requestType.equals(RequestType.CREATE)) {
            if (id != null) {
                throw new InvalidFilmFieldsException("id", "\"Id\" shouldn't be sent while creation");
            }
        } else if (requestType.equals(RequestType.UPDATE)) {
            if (id == null) {
                throw new InvalidFilmFieldsException(
                        "id",
                        "\"Id\" shouldn't be null in request for update film"
                );
            }
            if (id <= 0) {
                throw new InvalidFilmFieldsException(
                        "id",
                        String.format("\"Id\" isn't positive: %d", id)
                );
            }
        }
    }

    private void checkFilmReleaseDate(LocalDate releaseDate) {
        if (releaseDate
                .isBefore(LocalDate.of(1895, 12, 28))) {
            throw new InvalidFilmFieldsException(
                    "releaseDate",
                    String.format(
                            "\"Release Date\" must be after 1895-12-28: %s",
                            formatter.format(releaseDate)
                    )
            );
        }
    }

    public void checkIfGenrePresentById(Integer genreId) {
        filmGenreStorage.getGenreById(genreId);
    }


    public List<Film> checkIfAnyFilmPresentsByYear(Integer year) {
        return filmStorage.getAnyFilmByYear(year);
    }
}
