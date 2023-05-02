package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.user.UserStorage;
import ru.yandex.practicum.filmorate.util.TestDataProducer;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserTest {

    private static final String HOST = "http://localhost:";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    HttpHeaders applicationJsonHeaders;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    @Qualifier("H2UserRepository")
    private UserStorage userStorage;
    @Autowired
    private TestDataProducer testDataProducer;
    @Value(value = "${local.server.port}")
    private int port;
    private URI usersUrl;

    @BeforeEach
    public void beforeEach() {
        usersUrl = URI.create(String.format("%s%s/users", HOST, port));
        applicationJsonHeaders = new HttpHeaders();
        applicationJsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    private URI createGetUserByIdUrl(Long id) {
        return URI.create(String.format("%s%s/users/%d", HOST, port, id));
    }

    private URI createAddOrDeleteUserFriendUrl(Long currentUserId, Long friendId) {
        return URI.create(String.format("%s%s/users/%d/friends/%d", HOST, port, currentUserId, friendId));
    }

    private URI createGetUserFriendsUrl(Long id) {
        return URI.create(String.format("%s%s/users/%d/friends", HOST, port, id));
    }

    private URI createGetCommonFriendsUrl(Long userId, Long comparedUserId) {
        return URI.create(String.format("%s%s/users/%d/friends/common/%d", HOST, port, userId, comparedUserId));
    }

    // =============================== POST /users ======================================

    @Test
    public void shouldCreateUser() {
        User user = testDataProducer.getDefaultMutableUser();

        User createdUser = testRestTemplate.postForObject(usersUrl, user, User.class);

        user.setId(createdUser.getId());
        assertEquals(user, createdUser, "Created user doesn't match original user");
    }

    @Test
    public void shouldUseLoginForNameIfNameIsAbsentWhenUserCreating() {
        String body = "{" +
                "\"email\": \"email@domen.ru\"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        User createdUser = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class).getBody();

        assertEquals(createdUser.getLogin(), createdUser.getName(), "Name and login is not equal");
    }

    @Test
    public void shouldUseLoginForNameIfNameIsEmptyWhenUserCreating() {
        String body = "{" +
                "\"name\": \" \"," +
                "\"email\": \"email@domen.ru\"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        User createdUser = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class).getBody();

        assertEquals(createdUser.getLogin(), createdUser.getName(), "Name and login is not equal");
    }

    @Test
    public void shouldReturn400IfEmailIsAbsentWhenUserCreating() {
        String body = "{" +
                "\"name\": \"name\"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfEmailIsEmptyWhenUserCreating() {
        String body = "{" +
                "\"name\": \"name\"," +
                "\"email\": \" \"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfEmailNotContainsSymbolAtWhenUserCreating() {
        String body = "{" +
                "\"name\": \"name\"," +
                "\"email\": \"email&domen.ru\"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfLoginIsAbsentWhenUserCreating() {
        String body = "{" +
                "\"name\": \"name\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfLoginIsEmptyWhenUserCreating() {
        String body = "{" +
                "\"name\": \"name\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"login\": \" \"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfLoginIsContainsSpacesOutsideWhenUserCreating() {
        String body = "{" +
                "\"name\": \"name\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"login\": \"login \"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfLoginIsContainsSpacesInsideWhenUserCreating() {
        String body = "{" +
                "\"name\": \"name\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"login\": \"log in\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfBirthdayIsAbsentWhenUserCreating() {
        String body = "{" +
                "\"name\": \"name\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"login\": \"login\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfBirthdayIsEmptyWhenUserCreating() {
        String body = "{" +
                "\"name\": \"name\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"birthday\": \" \"," +
                "\"login\": \"login\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfBirthdayIsLaterWhenNowWhenUserCreating() {
        String body = "{" +
                "\"name\": \"name\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2100-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfIdIsSentInPostRequestWhenUserCreating() {
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"name\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }


    // =============================== PUT /users ======================================

    @Test
    public void shouldUpdateUser() {
        User user = testDataProducer.getDefaultMutableUser();
        Long createdUserId = userStorage.addUser(user).getId();
        user.setId(createdUserId);
        user.setName("new name");
        HttpEntity<User> entity = new HttpEntity<>(user, applicationJsonHeaders);

        User updatedUser = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        ).getBody();

        assertEquals(user, updatedUser, "User has not been updated");
    }

    @Test
    public void shouldUseLoginForNameIfNameIsAbsentWhenUserUpdating() {
        Long createdUserId = testDataProducer.addDefaultUserToDB();
        String body = "{" +
                "\"id\": " + createdUserId + "," +
                "\"email\": \"email@domen.ru\"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        User updatedUser = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        ).getBody();

        assertEquals(updatedUser.getName(), updatedUser.getLogin(), "Name doesn't equals login");
    }

    @Test
    public void shouldUseLoginForNameIfNameIsEmptyWhenUserUpdating() {
        Long createdUserId = testDataProducer.addDefaultUserToDB();
        String body = "{" +
                "\"id\": " + createdUserId + "," +
                "\"name\": \" \"," +
                "\"email\": \"email@domen.ru\"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        User updatedUser = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        ).getBody();

        assertEquals(updatedUser.getName(), updatedUser.getLogin(), "Name doesn't equals login");
    }

    @Test
    public void shouldReturn400IfEmailIsAbsentWhenUserUpdating() {
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"new name\"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfEmailIsEmptyWhenUserUpdating() {
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"new name\"," +
                "\"email\": \" \"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfEmailNotContainsSymbolAtWhenUserUpdating() {
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"new name\"," +
                "\"email\": \"emaildomen.ru\"," +
                "\"login\": \"login\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfLoginIsAbsentWhenUserUpdating() {
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"new name\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfLoginIsEmptyWhenUserUpdating() {
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"new name\"," +
                "\"login\": \" \"," +
                "\"email\": \"email@domen.ru\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfLoginIsContainsSpacesOutsideWhenUserUpdating() {
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"new name\"," +
                "\"login\": \"login \"," +
                "\"email\": \"email@domen.ru\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfLoginIsContainsSpacesInsideWhenUserUpdating() {
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"new name\"," +
                "\"login\": \"lo gin\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfBirthdayIsAbsentWhenUserUpdating() {
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"new name\"," +
                "\"login\": \"login\"," +
                "\"email\": \"email@domen.ru\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfBirthdayIsEmptyWhenUserUpdating() {
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"new name\"," +
                "\"login\": \"login\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"birthday\": \" \"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfBirthdayIsLaterWhenNowWhenUserUpdating() {
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"new name\"," +
                "\"login\": \"login\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"birthday\": \"2100-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfIdIsAbsentInRequestWhenUserUpdating() {
        String body = "{" +
                "\"name\": \"new name\"," +
                "\"login\": \"login\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn404IfIdIsWrongInRequestWhenUserUpdating() {
        String body = "{" +
                "\"id\": 9999," +
                "\"name\": \"new name\"," +
                "\"login\": \"login\"," +
                "\"email\": \"email@domen.ru\"," +
                "\"birthday\": \"2000-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    // =============================== GET /users ======================================

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldReturnUsers() {
        User user = testDataProducer.getDefaultMutableUser();
        user = userStorage.addUser(user);

        List<User> requestedUsers = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();

        assertEquals(1, requestedUsers.size(), "Wrong number of returned elements");
        assertTrue(requestedUsers.contains(user), "Expected element is not in List");
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldReturnEmptyListIfNoUsersCreated() {

        List<User> requestedUsers = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();

        assertEquals(0, requestedUsers.size(), "Wrong number of returned elements");
    }

    // =============================== GET /users/{id} ======================================

    @Test
    public void shouldReturnUserById() {
        User user = testDataProducer.getDefaultMutableUser();
        user = userStorage.addUser(user);

        User requestedUser = testRestTemplate.exchange(
                createGetUserByIdUrl(user.getId()),
                HttpMethod.GET,
                null,
                User.class
        ).getBody();

        assertEquals(user, requestedUser);
    }

    @Test
    public void shouldReturn400IfIdIsZero() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createGetUserByIdUrl(0L),
                HttpMethod.GET,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn400IfIdIsNegative() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createGetUserByIdUrl(-1L),
                HttpMethod.GET,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn404IfUserNotFoundById() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createGetUserByIdUrl(9999L),
                HttpMethod.GET,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    // =============================== PUT /users/{id}/friends/{friendId} ======================================

    @Test
    public void shouldAddFriendToUser() {
        Long currentUserId = testDataProducer.addDefaultUserToDB();
        Long friendId = testDataProducer.addDefaultUserToDB();


        testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(currentUserId, friendId),
                HttpMethod.PUT,
                null,
                String.class
        );

        User currentUser = userStorage.getUserById(currentUserId).get();
        User friend = userStorage.getUserById(friendId).get();
        List<User> currentUserFriends = userStorage.getUserFriends(currentUserId);
        List<User> friendFriends = userStorage.getUserFriends(friendId);
        assertTrue(currentUserFriends.contains(friend), "Friend not added");
        assertFalse(currentUserFriends.contains(currentUser), "Friendship is mutual, but should not");
    }

    @Test
    public void shouldReturn404IfUserIdIsZeroWhenAddFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(0L, 1L),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenAddFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(-1L, 1L),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn404IfFriendIdIsZeroWhenAddFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1L, 0L),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn404IfFriendIdIsNegativeWhenAddFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1L, -1L),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn404IfUserIsAbsentWhenAddFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(9999L, 2L),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn404IfFriendIsAbsentWhenAddFriend() {
        testDataProducer.addDefaultUserToDB();

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1L, 9999L),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    // =============================== DELETE /users/{id}/friends/{friendId} ======================================

    @Test
    public void shouldDeleteFriendFromUserIfOneWayFriendship() {
        Long currentUserId = testDataProducer.addDefaultUserToDB();
        Long friendId = testDataProducer.addDefaultUserToDB();
        userStorage.addFriendToUser(currentUserId, friendId);

        testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(currentUserId, friendId),
                HttpMethod.DELETE,
                null,
                User.class
        );

        List<User> currentUserFriends = userStorage.getUserFriends(currentUserId);
        List<User> friendFriends = userStorage.getUserFriends(friendId);
        User currentUser = userStorage.getUserById(currentUserId).get();
        User friend = userStorage.getUserById(friendId).get();
        assertFalse(currentUserFriends.contains(friend), "Friend not deleted");
        assertFalse(friendFriends.contains(currentUser), "Friendship is mutual, but should not");
    }

    @Test
    public void shouldDeleteFriendFromUserIfMutualFriendship() {
        Long currentUserId = testDataProducer.addDefaultUserToDB();
        Long friendId = testDataProducer.addDefaultUserToDB();
        userStorage.addFriendToUser(currentUserId, friendId);
        userStorage.addFriendToUser(friendId, currentUserId);

        testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(currentUserId, friendId),
                HttpMethod.DELETE,
                null,
                User.class
        );

        List<User> currentUserFriends = userStorage.getUserFriends(currentUserId);
        List<User> friendFriends = userStorage.getUserFriends(friendId);
        User currentUser = userStorage.getUserById(currentUserId).get();
        User friend = userStorage.getUserById(friendId).get();
        assertFalse(currentUserFriends.contains(friend), "Friend not deleted");
        assertTrue(friendFriends.contains(currentUser), "Friend should not been deleted");
    }

    @Test
    public void shouldReturn404IfUserIdIsZeroWhenDeleteFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(0L, 1L),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenDeleteFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(-1L, 1L),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn404IfFriendIdIsZeroWhenDeleteFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1L, 0L),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn404IfFriendIdIsNegativeWhenDeleteFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1L, -1L),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn404IfUserIsAbsentWhenDeleteFriend() {
        testDataProducer.addDefaultUserToDB();

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(9999L, 1L),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn404IfFriendIsAbsentWhenDeleteFriend() {
        testDataProducer.addDefaultUserToDB();

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1L, 9999L),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status");
    }

    // =============================== GET /users/{id}/friends ======================================

    @Test
    public void shouldReturnUserFriends() {
        Long currentUserId = testDataProducer.addDefaultUserToDB();
        Long friendId = testDataProducer.addDefaultUserToDB();
        userStorage.addFriendToUser(currentUserId, friendId);

        List<User> currentUserFriends = testRestTemplate.exchange(
                createGetUserFriendsUrl(currentUserId),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();
        List<User> userFriendFriends = testRestTemplate.exchange(
                createGetUserFriendsUrl(friendId),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();

        User friend = userStorage.getUserById(friendId).get();
        User currentUser = userStorage.getUserById(currentUserId).get();
        assertTrue(currentUserFriends.contains(friend), "Wrong friends list size");
        assertFalse(userFriendFriends.contains(currentUser), "Wrong friends list size");
    }

    @Test
    public void shouldReturnEmptyListWhenNoFriends() {
        Long currentUserId = testDataProducer.addDefaultUserToDB();

        List<User> requestedUsers = testRestTemplate.exchange(
                createGetUserFriendsUrl(currentUserId),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();

        assertTrue(requestedUsers.isEmpty(), "Wrong friends list size");
    }

    @Test
    public void shouldReturn400IfUserIdIsZeroWhenRequestingUserFriendsList() {

        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetUserFriendsUrl(0L),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status");
    }

    @Test
    public void shouldReturn400IfUserIdIsNegativeWhenRequestingUserFriendsList() {

        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetUserFriendsUrl(-1L),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status");
    }

    // =============================== GET /users/{id}/friends/common/{otherId} ====================================
    @Test
    public void shouldReturnCommonFriendsList() {
        Long firstUserId = testDataProducer.addDefaultUserToDB();
        Long secondUserId = testDataProducer.addDefaultUserToDB();
        Long commonFriendId = testDataProducer.addDefaultUserToDB();
        userStorage.addFriendToUser(firstUserId, commonFriendId);
        userStorage.addFriendToUser(secondUserId, commonFriendId);

        List<User> requestedUsers = testRestTemplate.exchange(
                createGetCommonFriendsUrl(firstUserId, secondUserId),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();

        User commonFriend = userStorage.getUserById(commonFriendId).get();
        assertEquals(1, requestedUsers.size(), "Wrong common friends count");
        assertTrue(requestedUsers.contains(commonFriend), "Common friend is not in the list");

    }

    @Test
    public void shouldReturn400IfUserIdIsZeroWhenGetCommonFriends() {
        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetCommonFriendsUrl(0L, 1L),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfUserIdIsNegativeWhenGetCommonFriends() {
        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetCommonFriendsUrl(-1L, 1L),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfComparedUserIdIsZeroWhenGetCommonFriends() {
        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetCommonFriendsUrl(0L, 1L),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfComparedUserIdIsNegativeWhenGetCommonFriends() {
        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetCommonFriendsUrl(-1L, 1L),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturnEmptyListIfNoCommonFriends() {
        Long firstUserId = testDataProducer.addDefaultUserToDB();
        Long secondUserId = testDataProducer.addDefaultUserToDB();

        List<User> requestedUsers = testRestTemplate.exchange(
                createGetCommonFriendsUrl(firstUserId, secondUserId),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();

        assertTrue(requestedUsers.isEmpty(), "Common friend found but shouldn not");
    }

}
