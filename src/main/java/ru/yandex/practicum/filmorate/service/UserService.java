package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotExistException;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserStorage userStorage;
    private final UserFieldsValidator userFieldsValidator;

    public UserService(UserStorage userStorage, UserFieldsValidator userFieldsValidator) {
        this.userStorage = userStorage;
        this.userFieldsValidator = userFieldsValidator;
    }

    public User addUser(User user) {
        userFieldsValidator.checkUserFields(user, RequestType.CREATE);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        userFieldsValidator.checkUserFields(user, RequestType.UPDATE);
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addUserFriend(Long currentUserId, Long newFriendId) {
        userFieldsValidator.checkIfPresentById(currentUserId);
        userFieldsValidator.checkIfPresentById(newFriendId);
        User currentUser = getUserFromStorageById(currentUserId);
        User newFriend = getUserFromStorageById(newFriendId);

        currentUser.getFriends().add(newFriendId);
        newFriend.getFriends().add(currentUserId);
    }

    public void removeUserFriend(Long currentUserId, Long friendId) {
        User currentUser = getUserFromStorageById(currentUserId);
        User newFriend = getUserFromStorageById(friendId);

        currentUser.getFriends().remove(friendId);
        newFriend.getFriends().remove(currentUserId);
    }

    public List<User> getFriendsForUser(Long currentUserId) {
        User currentUser = getUserFromStorageById(currentUserId);
        return currentUser.getFriends().stream()
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long currentUserId, Long comparedUserId) {
        User currentUser = getUserFromStorageById(currentUserId);
        User comparedUser = getUserFromStorageById(comparedUserId);

        return currentUser.getFriends().stream()
                .filter(id -> comparedUser.getFriends().contains(id))
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public User getUserFromStorageById(Long userId) {
        Optional<User> requestedUser = userStorage.getUserById(userId);
        if (requestedUser.isEmpty()) {
            throw new UserNotExistException(
                    String.format("User with id %d doesn't exist", userId)
            );
        }
        return requestedUser.get();
    }

}
