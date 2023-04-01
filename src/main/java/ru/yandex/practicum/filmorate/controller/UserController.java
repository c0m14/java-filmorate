package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.InvalidUserFieldsException;
import ru.yandex.practicum.filmorate.exception.UserNotExistException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;


@Slf4j
@Validated
@RestController
@RequestMapping(value = "/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) throws InvalidUserFieldsException {
        log.debug("Got request to create user: {}", user);
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) throws InvalidUserFieldsException, UserNotExistException {
        log.debug("Got request to update user: {}", user);
        return userService.updateUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Got request to get all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User findUserById(@Valid @PathVariable("id") @Min(1) Long id) {
        log.debug("Got request to find user with id: {}", id);
        return userService.getUserFromStorageById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addUserFriend(
            @Valid
            @PathVariable("id") @Min(1) Long userId,
            @PathVariable("friendId") @Min(1) Long friendId
    ) {
        userService.addUserFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriendFromUser(
            @Valid
            @PathVariable("id") @Min(1) Long userId,
            @PathVariable("friendId") @Min(1) Long friendId
    ) {
        userService.removeUserFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriendsForUser(
            @Valid
            @PathVariable("id") @Min(1) Long userId

    ) {
        return userService.getFriendsForUser(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(
            @Valid
            @PathVariable("id") @Min(1) Long userId,
            @PathVariable("otherId") @Min(1) Long otherUserId
    ) {
        return userService.getCommonFriends(userId, otherUserId);
    }
}
