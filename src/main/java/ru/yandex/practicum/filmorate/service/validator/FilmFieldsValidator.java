package ru.yandex.practicum.filmorate.service.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidFilmFieldsException;
import ru.yandex.practicum.filmorate.exception.UserNotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FilmFieldsValidator {

    private final FilmStorage filmStorage;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");;

    public void checkRequestFilm(Film film, RequestType requestType) throws InvalidFilmFieldsException {
        checkFilmId(film.getId(), requestType);
        if (requestType.equals(RequestType.UPDATE)) {
            checkIfPresent(film);
        }
        checkFilmReleaseDate(film.getReleaseDate());
    }

    public void checkIfPresent(Film film) {
        if (filmStorage.getFilmById(film.getId()).isEmpty()) {
            throw new UserNotExistException(
                    String.format("Film with id %d doesn't exist", film.getId())
            );
        }
    }

    public void checkIfPresentById(Long filmId) {
        if (filmStorage.getFilmById(filmId).isEmpty()) {
            throw new UserNotExistException(
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

}
