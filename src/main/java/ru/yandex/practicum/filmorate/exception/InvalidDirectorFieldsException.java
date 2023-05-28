package ru.yandex.practicum.filmorate.exception;

public class InvalidDirectorFieldsException extends InvalidFieldsException {
    public InvalidDirectorFieldsException(String fieldName, String message) {
        super(fieldName, message);
    }
}
