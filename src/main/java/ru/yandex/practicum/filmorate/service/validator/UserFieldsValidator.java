package ru.yandex.practicum.filmorate.service.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidUserFieldsException;
import ru.yandex.practicum.filmorate.exception.UserNotExistException;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.user.UserStorage;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFieldsValidator {
    private final UserStorage userStorage;

    public void checkUserFields(User user, RequestType requestType) throws InvalidUserFieldsException {
        checkUserId(user.getId(), requestType);
        if (requestType.equals(RequestType.UPDATE)) {
            checkIfPresent(user);
        }
        checkUserLogin(user.getLogin());
        checkUserName(user);
    }

    public void checkIfPresent(User user) {
        if (userStorage.getUserById(user.getId()).isEmpty()) {
            throw new UserNotExistException(
                    String.format("User with id %d doesn't exist", user.getId())
            );
        }
    }

    public void checkIfPresentById(Long userId) {
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new UserNotExistException(
                    String.format("User with id %d doesn't exist", userId)
            );
        }
    }

    private void checkUserId(Long id, RequestType requestType) {
        if (requestType.equals(RequestType.CREATE)) {
            if (id != null) {
                throw new InvalidUserFieldsException("Id", "\"Id\" shouldn't be sent while creation");
            }
        } else if (requestType.equals(RequestType.UPDATE)) {
            if (id == null) {
                throw new InvalidUserFieldsException(
                        "id",
                        "\"Id\" shouldn't be empty in update request"
                );
            }
            if (id <= 0) {
                throw new InvalidUserFieldsException(
                        "id",
                        String.format("\"Id\" isn't positive: %d", id)
                );
            }
        }
    }

    private void checkUserLogin(String login) {
        if (login.contains(" ")) {
            throw new InvalidUserFieldsException(
                    "login",
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
