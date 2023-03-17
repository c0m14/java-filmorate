package ru.yandex.practicum.filmorate.exceptions;

public class InvalidFilmFieldsException extends RuntimeException {
    public InvalidFilmFieldsException(String message) {
        super(message);
    }
}
