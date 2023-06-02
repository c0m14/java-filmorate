package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

public class NotExistsException extends RuntimeException {
    @Getter
    private final String fieldName;

    public NotExistsException (String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }
}
