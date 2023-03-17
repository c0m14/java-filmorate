package ru.yandex.practicum.filmorate.exceptions;

public class FilmNotExistException extends RuntimeException {
    public FilmNotExistException(String message) {
        super(message);
    }
}
