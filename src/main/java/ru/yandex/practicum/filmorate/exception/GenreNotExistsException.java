package ru.yandex.practicum.filmorate.exception;

public class GenreNotExistsException extends RuntimeException {
    public GenreNotExistsException(String message) {
        super(message);
    }
}
