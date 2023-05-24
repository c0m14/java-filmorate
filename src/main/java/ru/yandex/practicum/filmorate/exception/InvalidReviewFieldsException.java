package ru.yandex.practicum.filmorate.exception;

public class InvalidReviewFieldsException extends InvalidFieldsException {
    public InvalidReviewFieldsException(String filedName, String message) {
        super(filedName, message);
    }
}
