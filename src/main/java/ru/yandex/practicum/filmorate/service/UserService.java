package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotExistException;
import ru.yandex.practicum.filmorate.model.FriendConfirmationStatus;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;
import ru.yandex.practicum.filmorate.repository.user.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final UserFieldsValidator userFieldsValidator;

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
        User currentUser = getUserFromStorageById(currentUserId);
        User newFriend = getUserFromStorageById(newFriendId);

        currentUser.getFriends().put(newFriendId, FriendConfirmationStatus.WAITING_FOR_APPROVAL);
        newFriend.getFriends().put(currentUserId, FriendConfirmationStatus.WAITING_FOR_APPROVAL);
    }

    public void removeUserFriend(Long currentUserId, Long friendId) {
        User currentUser = getUserFromStorageById(currentUserId);
        User newFriend = getUserFromStorageById(friendId);

        currentUser.getFriends().remove(friendId);
        newFriend.getFriends().remove(currentUserId);
    }

    public List<User> getFriendsForUser(Long currentUserId) {
        User currentUser = getUserFromStorageById(currentUserId);
        return currentUser.getFriends().keySet().stream()
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long currentUserId, Long comparedUserId) {
        User currentUser = getUserFromStorageById(currentUserId);
        User comparedUser = getUserFromStorageById(comparedUserId);

        return currentUser.getFriends().keySet().stream()
                .filter(id -> comparedUser.getFriends().containsKey(id))
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public User getUserFromStorageById(Long userId) {
        return userStorage.getUserById(userId).orElseThrow(
                () -> new UserNotExistException(String.format("User with id %d doesn't exist", userId))
        );
    }

}
