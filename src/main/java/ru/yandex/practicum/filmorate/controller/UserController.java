package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.InvalidFilmFieldsException;
import ru.yandex.practicum.filmorate.exceptions.InvalidUserFieldsException;
import ru.yandex.practicum.filmorate.exceptions.UserNotExistException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping(value = "/users")
public class UserController {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Map<Integer, User> users = new HashMap<>();
    private int idCounter = 1;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) throws InvalidUserFieldsException {
        log.debug("Got request to create user: {}", user);
        checkUserFields(user, RequestType.CREATE);
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) throws InvalidUserFieldsException, UserNotExistException {
        log.debug("Got request to update user: {}", user);
        checkUserFields(user, RequestType.UPDATE);
        if (!users.containsKey(user.getId())) {
            log.error("User with id {} doesn't exist", user.getId());
            throw new UserNotExistException(
                    String.format("User with id %d doesn't exist", user.getId())
            );
        }
        users.put(user.getId(), user);
        return user;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    private void checkUserFields(User user, RequestType requestType) throws InvalidUserFieldsException {
        checkUserId(user.getId(), requestType);
        checkUserLogin(user.getLogin());
        checkUserName(user);
    }

    private void checkUserId(int id, RequestType requestType) throws InvalidUserFieldsException {
        if (requestType.equals(RequestType.CREATE)) {
            if (id != 0) {
                log.error("\"Id\" shouldn't be sent while creation");
                throw new InvalidFilmFieldsException("\"Id\" shouldn't be sent while creation");
            }
        } else if (requestType.equals(RequestType.UPDATE)) {
            if (id <= 0) {
                log.error("\"Id\" isn't positive: {}", id);
                throw new InvalidFilmFieldsException(
                        String.format("\"Id\" isn't positive: %d", id));
            }
        }
    }

    private void checkUserLogin(String login) throws InvalidUserFieldsException {
        if (login.contains(" ")) {
            log.error("\"Login\" shouldn't contain spaces: {}", login);
            throw new InvalidUserFieldsException(
                    String.format("\"Login\" shouldn't contain spaces: %s", login)
            );
        }
    }

    private void checkUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Name is empty, login used instead");
            user.setName(user.getLogin());
        }
    }
}
