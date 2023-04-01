package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    void setIdCount(User user);

    User addUser(User user);

    User updateUser(User user);

    List<User> getAllUsers();

    Optional<User> getUserById(Long id);
}
