package ru.yandex.practicum.filmorate.exception;

public class InvalidUserFieldsException extends InvalidFieldsException {
    public InvalidUserFieldsException(String filedName, String message) {
        super(filedName, message);
    }
}
