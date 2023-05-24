package ru.yandex.practicum.filmorate.exception;

public class ReviewLikeNotExistsException extends RuntimeException {
    public ReviewLikeNotExistsException(String message) {
        super(message);
    }
}
