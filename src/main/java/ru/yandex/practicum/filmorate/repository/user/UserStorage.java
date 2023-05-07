package ru.yandex.practicum.filmorate.repository.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    List<User> getCommonFriends(Long userId, Long otherUserId);

    void addFriendToUser(Long userId, Long friendId);

    List<User> getUserFriends(Long userId);

    boolean removeFriendFromUser(Long userId, Long friendId);
}
