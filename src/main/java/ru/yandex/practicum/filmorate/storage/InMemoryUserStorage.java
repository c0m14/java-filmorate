package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotExistException;
import ru.yandex.practicum.filmorate.exception.InvalidFilmFieldsException;
import ru.yandex.practicum.filmorate.exception.InvalidUserFieldsException;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.model.User;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private Map<Integer, User> users;
    private final DateTimeFormatter formatter;
    private int idCounter;

    public InMemoryUserStorage() {
        users = new HashMap<>();
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        idCounter = 1;
    }

    @Override
    public void setIdCount(User user) {
        user.setId(idCounter);
        idCounter++;
    }

    @Override
    public User addUser(User user) {
        checkUserFields(user, RequestType.CREATE);
        setIdCount(user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        checkUserFields(user, RequestType.UPDATE);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    private void checkUserFields(User user, RequestType requestType) throws InvalidUserFieldsException {
        if (requestType.equals(RequestType.UPDATE)) {
            checkIfPresent(user);
        }
        checkUserId(user.getId(), requestType);
        checkUserLogin(user.getLogin());
        checkUserName(user);
    }

    private void checkIfPresent(User user) {
        if (!users.containsKey(user.getId())) {
            log.error("Film with id {} doesn't exist", user.getId());
            throw new FilmNotExistException(
                    String.format("Film with id %d doesn't exist", user.getId())
            );
        }
    }

    private void checkUserId(Integer id, RequestType requestType) throws InvalidUserFieldsException {
        if (requestType.equals(RequestType.CREATE)) {
            if (id != null) {
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
