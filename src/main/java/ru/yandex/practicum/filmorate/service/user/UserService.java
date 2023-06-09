package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotExistsException;
import ru.yandex.practicum.filmorate.model.RequestType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.user.UserStorage;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("H2UserRepository")
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
        userFieldsValidator.checkIfPresentById(currentUserId);
        userFieldsValidator.checkIfPresentById(newFriendId);

        userStorage.addFriendToUser(currentUserId, newFriendId);
    }

    public void removeUserFriend(Long currentUserId, Long friendId) {
        userFieldsValidator.checkIfPresentById(currentUserId);
        userFieldsValidator.checkIfPresentById(friendId);

        userStorage.removeFriendFromUser(currentUserId, friendId);
    }

    public List<User> getFriendsForUser(Long currentUserId) {
        userFieldsValidator.checkIfPresentById(currentUserId);

        return userStorage.getUserFriends(currentUserId);
    }

    public List<User> getCommonFriends(Long currentUserId, Long comparedUserId) {
        userFieldsValidator.checkIfPresentById(currentUserId);
        userFieldsValidator.checkIfPresentById(comparedUserId);

        return userStorage.getCommonFriends(currentUserId, comparedUserId);
    }

    public User getUserFromStorageById(Long userId) {
        return userStorage.getUserById(userId).orElseThrow(
                () -> new NotExistsException(
                        "User",
                        String.format("User with id %d does not exist", userId)
                ));
    }

    public void removeUserById(Long userId) {
        userFieldsValidator.checkIfPresentById(userId);
        userStorage.removeUserById(userId);
    }
}
