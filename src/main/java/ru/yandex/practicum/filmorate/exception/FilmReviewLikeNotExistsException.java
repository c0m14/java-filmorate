package ru.yandex.practicum.filmorate.exception;

public class FilmReviewLikeNotExistsException extends RuntimeException {
    public FilmReviewLikeNotExistsException(String message) {
        super(message);
    }
}
