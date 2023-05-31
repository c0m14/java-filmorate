package ru.yandex.practicum.filmorate.exception;

public class FilmReviewDislikeNotExistsException extends RuntimeException {
    public FilmReviewDislikeNotExistsException(String message) {
        super(message);
    }
}
