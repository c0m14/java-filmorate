package ru.yandex.practicum.filmorate.exception;

public class InvalidUserFieldsException extends RuntimeException {
    public InvalidUserFieldsException(String message) {
        super(message);
    }
}
