package ru.yandex.practicum.filmorate.exception;

public class ReviewDislikeNotExistsException extends RuntimeException {
    public ReviewDislikeNotExistsException(String message) {
        super(message);
    }
}
