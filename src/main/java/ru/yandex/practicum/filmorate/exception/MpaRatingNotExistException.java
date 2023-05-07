package ru.yandex.practicum.filmorate.exception;

public class MpaRatingNotExistException extends RuntimeException {
    public MpaRatingNotExistException(String message) {
        super(message);
    }
}
