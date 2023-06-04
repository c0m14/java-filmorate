package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

public class InvalidFieldsException extends RuntimeException {
    @Getter
    String fieldName;
    @Getter
    String className;

    public InvalidFieldsException(String className, String fieldName, String message) {
        super(message);
        this.className = className;
        this.fieldName = fieldName;
    }
}
