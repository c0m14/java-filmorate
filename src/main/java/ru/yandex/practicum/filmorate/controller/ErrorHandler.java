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
        return new ErrorResponse(e.getParameter(), e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotExistsException(NotExistsException e) {
        log.error(e.getMessage());
        return new ErrorResponse(e.getFieldName(), e.getMessage());
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

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleFeedNotExistException(FeedStorageEmptyException e) {
        log.error(e.getMessage());
        return new ErrorResponse("feed", e.getMessage());
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