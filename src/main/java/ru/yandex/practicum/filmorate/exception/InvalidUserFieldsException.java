package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

public class InvalidUserFieldsException extends InvalidFieldsException {
    public InvalidUserFieldsException(String filedName, String message) {
        super(filedName, message);
    }
}
