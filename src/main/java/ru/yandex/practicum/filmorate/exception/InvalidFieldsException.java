package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

public class InvalidFieldsException extends RuntimeException {
    @Getter
    String fieldName;

    public InvalidFieldsException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }
}
