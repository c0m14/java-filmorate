package ru.yandex.practicum.filmorate.exception;

public class FeedStorageEmptyException extends RuntimeException {
    public FeedStorageEmptyException(String message) {
        super(message);
    }
}
