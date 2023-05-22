package ru.yandex.practicum.filmorate.exception;

public class ReviewNotExistsException extends RuntimeException {
    public ReviewNotExistsException(String message) {
        super(message);
    }
}
