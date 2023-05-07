package ru.yandex.practicum.filmorate.repository.user.inMemory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.user.UserStorage;

import java.util.*;

@Component
@Qualifier("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long idCounter = 1L;

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

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void addFriendToUser(Long userId, Long friendId) {
        throw  new UnsupportedOperationException();
    }

    @Override
    public List<User> getUserFriends(Long userId) {
        throw  new UnsupportedOperationException();
    }

    @Override
    public boolean removeFriendFromUser(Long userId, Long friendId) {
        throw  new UnsupportedOperationException();
    }
}
