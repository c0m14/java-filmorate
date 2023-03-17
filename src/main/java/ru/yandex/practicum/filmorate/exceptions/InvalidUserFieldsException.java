package ru.yandex.practicum.filmorate.exceptions;

public class InvalidUserFieldsException extends RuntimeException {
    public InvalidUserFieldsException(String message) {
        super(message);
    }
}
