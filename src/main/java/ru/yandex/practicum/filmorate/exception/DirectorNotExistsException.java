package ru.yandex.practicum.filmorate.exception;

public class DirectorNotExistsException extends RuntimeException {
    public DirectorNotExistsException(String message) {
        super(message);
    }
}