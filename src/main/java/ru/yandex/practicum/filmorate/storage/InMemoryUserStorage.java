package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users;
    private Long idCounter;

    public InMemoryUserStorage() {
        users = new HashMap<>();
        idCounter = 1L;
    }

    private void setIdCount(User user) {
        user.setId(idCounter);
        idCounter++;
    }

    @Override
    public User addUser(User user) {
        setIdCount(user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }
}
