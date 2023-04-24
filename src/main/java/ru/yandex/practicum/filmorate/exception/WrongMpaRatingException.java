package ru.yandex.practicum.filmorate.exception;

public class WrongMpaRatingException extends RuntimeException {
    public WrongMpaRatingException(String message) {
        super(message);
    }
}
