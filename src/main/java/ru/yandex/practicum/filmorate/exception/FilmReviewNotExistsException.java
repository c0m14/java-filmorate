package ru.yandex.practicum.filmorate.exception;

public class FilmReviewNotExistsException extends RuntimeException {
    public FilmReviewNotExistsException(String message) {
        super(message);
    }
}
