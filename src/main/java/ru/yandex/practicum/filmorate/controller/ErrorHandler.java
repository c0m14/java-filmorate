package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.*;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseList handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        return new ErrorResponseList(
                e.getBindingResult().getFieldErrors().stream()
                        .map(fieldError -> new ErrorResponse(fieldError.getField(), fieldError.getDefaultMessage()))
                        .collect(Collectors.toList())
        );

    }

    @ExceptionHandler({
            InvalidUserFieldsException.class,
            InvalidFilmFieldsException.class,
            InvalidFilmReviewFieldsException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidFieldsExceptionWithManualValidation(InvalidFieldsException e) {
        log.error(e.getMessage());
        return new ErrorResponse(e.getFieldName(), e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectParameterException(final IncorrectParameterException e) {
        return new ErrorResponse("sortBy",
                String.format("Ошибка с полем \"%s\".", e.getParameter())
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotExistException(UserNotExistException e) {
        log.error(e.getMessage());
        return new ErrorResponse("userId", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleFilmNotExistException(FilmNotExistException e) {
        log.error(e.getMessage());
        return new ErrorResponse("filmId", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleFilmGenreNotExistException(GenreNotExistsException e) {
        log.error(e.getMessage());
        return new ErrorResponse("genres", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleFilmMpaRatingNotExistException(MpaRatingNotExistException e) {
        log.error(e.getMessage());
        return new ErrorResponse("mpa", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDirectorNotExistsException(DirectorNotExistsException e) {
        log.error(e.getMessage());
        return new ErrorResponse("director", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleReviewNotExistException(FilmReviewNotExistsException e) {
        log.error(e.getMessage());
        return new ErrorResponse("reviewId", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleReviewLikeNotExistException(FilmReviewLikeNotExistsException e) {
        log.error(e.getMessage());
        return new ErrorResponse("reviewLike record", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleReviewDislikeNotExistException(FilmReviewDislikeNotExistsException e) {
        log.error(e.getMessage());
        return new ErrorResponse("reviewDislike record", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseList handleConstraintViolationException(ConstraintViolationException e) {
        log.error(e.getMessage());
        return new ErrorResponseList(
                e.getConstraintViolations().stream()
                        .map(violation -> new ErrorResponse(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        ))
                        .collect(Collectors.toList())
        );
    }

    @Getter
    @AllArgsConstructor
    private class ErrorResponseList {

        private final List<ErrorResponse> errorResponses;

    }

    @Getter
    @AllArgsConstructor
    private class ErrorResponse {

        private String fieldName;
        private String errorDescription;

    }
}
