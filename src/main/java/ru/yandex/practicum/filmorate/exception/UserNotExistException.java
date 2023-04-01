package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

public class UserNotExistException extends RuntimeException {
    public UserNotExistException(String message) {
        super(message);
    }
}
