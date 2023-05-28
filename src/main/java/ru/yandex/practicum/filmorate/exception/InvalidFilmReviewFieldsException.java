package ru.yandex.practicum.filmorate.exception;

public class InvalidFilmReviewFieldsException extends InvalidFieldsException {
    public InvalidFilmReviewFieldsException(String filedName, String message) {
        super(filedName, message);
    }
}
