package ru.yandex.practicum.filmorate.exception;

public class InvalidFilmFieldsException extends InvalidFieldsException {
    public InvalidFilmFieldsException(String fieldName, String message) {
        super(fieldName, message);
    }
}
