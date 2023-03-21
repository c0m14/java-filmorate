package ru.yandex.practicum.filmorate.exception;

public class FilmNotExistException extends RuntimeException {
    public FilmNotExistException(String message) {
        super(message);
    }
}
