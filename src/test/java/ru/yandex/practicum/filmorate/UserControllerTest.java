package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import ru.yandex.practicum.filmorate.model.User;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    private static final String HOST = "http://localhost:";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    HttpHeaders applicationJsonHeaders;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Value(value = "${local.server.port}")
    private int PORT;
    private URI usersUrl;

    @BeforeEach
    public void beforeEach() {
        usersUrl = URI.create(String.format("%s%s/users", HOST, PORT));
        applicationJsonHeaders = new HttpHeaders();
        applicationJsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    private URI createGetUserByIdUrl(int id) {
        return URI.create(String.format("%s%s/users/%d", HOST, PORT, id));
    }

    private URI createAddOrDeleteUserFriendUrl(int currentUserId, int friendId) {
        return URI.create(String.format("%s%s/users/%d/friends/%d", HOST, PORT, currentUserId, friendId));
    }

    private URI createGetUserFriendsUrl(int id) {
        return URI.create(String.format("%s%s/users/%d/friends", HOST, PORT, id));
    }

    private URI createGetCommonFriendsUrl(int userId, int comparedUserId) {
        return URI.create(String.format("%s%s/users/%d/friends/common/%d", HOST, PORT, userId, comparedUserId));
    }

    // =============================== POST /users ======================================

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldCreateUser() {
        User user = new User(
                "Name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );

        User createdUser = testRestTemplate.postForObject(usersUrl, user, User.class);

        user.setId(1L);
        assertEquals(user, createdUser, "Created user doesn't match original user");
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldIncrementIdWhenUserCreating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );

        User createdUser = testRestTemplate.postForObject(usersUrl, user, User.class);

        assertEquals(1L, createdUser.getId());
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

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldUpdateUser() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        user.setId(1L);
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

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldIncrementIdWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        user.setId(1L);
        user.setName("new name");
        HttpEntity<User> entity = new HttpEntity<>(user, applicationJsonHeaders);

        User updatedUser = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        ).getBody();

        assertEquals(user.getId(), updatedUser.getId(), "Wrong id");
    }

    @Test
    public void shouldUseLoginForNameIfNameIsAbsentWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = "{" +
                "\"id\": 1," +
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = "{" +
                "\"id\": 1," +
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
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
    public void shouldReturn500IfIdIsAbsentInRequestWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
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

        assertEquals(HttpStatus.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfIdIsWrongInRequestWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = "{" +
                "\"id\": 3," +
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
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        user.setId(1L);

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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturnUserById() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        user.setId(1L);

        User requestedUser = testRestTemplate.exchange(
                createGetUserByIdUrl(1),
                HttpMethod.GET,
                null,
                User.class
        ).getBody();

        assertEquals(user, requestedUser);
    }

    @Test
    public void shouldReturn400IfIdIsZero() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createGetUserByIdUrl(0),
                HttpMethod.GET,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfIdIsNegative() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createGetUserByIdUrl(-1),
                HttpMethod.GET,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfUserNotFoundById() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createGetUserByIdUrl(1),
                HttpMethod.GET,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode());
    }

    // =============================== PUT /users/{id}/friends/{friendId} ======================================

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldAddFriendToUser() {
        User currentUser = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, currentUser, User.class);
        User friend = new User(
                "name2",
                "login2",
                "email2@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, friend, User.class);

        testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1, 2),
                HttpMethod.PUT,
                null,
                String.class
        );

        currentUser = testRestTemplate.getForObject(
                createGetUserByIdUrl(1),
                User.class
        );
        friend = testRestTemplate.getForObject(
                createGetUserByIdUrl(2),
                User.class
        );
        assertTrue(currentUser.getFriends().contains(friend.getId()));
        assertTrue(friend.getFriends().contains(currentUser.getId()));
    }

    @Test
    public void shouldReturn400IfUserIdIsZeroWhenAddFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(0, 1),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfUserIdIsNegativeWhenAddFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(-1, 1),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfFriendIdIsZeroWhenAddFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1, 0),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfFriendIdIsNegativeWhenAddFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1, -1),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfUserIsAbsentWhenAddFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1, 2),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfFriendIsAbsentWhenAddFriend() {
            User currentUser = new User(
                    "name",
                    "login",
                    "email@domen.ru",
                    LocalDate.of(2000, 1, 1)
            );
            testRestTemplate.postForObject(usersUrl, currentUser, User.class);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1, 2),
                HttpMethod.PUT,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode());
    }

    // =============================== DELETE /users/{id}/friends/{friendId} ======================================

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldDeleteFriendFromUser() {
        User currentUser = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        currentUser.setFriends(Set.of(2L));
    }

    @Test
    public void shouldReturn400IfUserIdIsZeroWhenDeleteFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(0, 1),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfUserIdIsNegativeWhenDeleteFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(-1, 1),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfFriendIdIsZeroWhenDeleteFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1, 0),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfFriendIdIsNegativeWhenDeleteFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1, -1),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfUserIsAbsentWhenDeleteFriend() {

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1, 2),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfFriendIsAbsentWhenDeleteFriend() {
        User currentUser = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, currentUser, User.class);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1, 2),
                HttpMethod.DELETE,
                null,
                User.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode());
    }

    // =============================== GET /users/{id}/friends ======================================

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturnUserFriends() {
        User currentUser = new User(
                "user",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, currentUser, User.class);
        User friend = new User(
                "friend",
                "login2",
                "email2@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, friend, User.class);
        testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1, 2),
                HttpMethod.PUT,
                null,
                User.class
        );

        List<User> currentUserFriends = testRestTemplate.exchange(
                createGetUserFriendsUrl(1),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();
        List<User> userFriendFriends = testRestTemplate.exchange(
                createGetUserFriendsUrl(2),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();
        friend = testRestTemplate.getForObject(
                createGetUserByIdUrl(2),
                User.class
        );
        currentUser = testRestTemplate.getForObject(
                createGetUserByIdUrl(1),
                User.class
        );
        assertTrue(currentUserFriends.contains(friend));
        assertTrue(userFriendFriends.contains(currentUser));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturnEmptyListWhenNoFriends() {
        User currentUser = new User(
                "user",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, currentUser, User.class);

        List<User> requestedUsers = testRestTemplate.exchange(
                createGetUserFriendsUrl(1),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();

        assertTrue(requestedUsers.isEmpty());
    }

    @Test
    public void shouldReturn400IfUserIdIsZeroWhenRequestingUserFriendsList() {

        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetUserFriendsUrl(0),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfUserIdIsNegativeWhenRequestingUserFriendsList() {

        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetUserFriendsUrl(-1),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    // =============================== GET /users/{id}/friends/common/{otherId} ====================================
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturnCommonFriendsList() {
        User currentUser = new User(
                "user",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, currentUser, User.class);
        User friend = new User(
                "friend",
                "friend",
                "friend@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, friend, User.class);
        User common = new User(
                "common",
                "common",
                "common@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, common, User.class);
        testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(1, 3),
                HttpMethod.PUT,
                null,
                User.class
        );
        testRestTemplate.exchange(
                createAddOrDeleteUserFriendUrl(2, 3),
                HttpMethod.PUT,
                null,
                User.class
        );

        List<User> requestedUsers = testRestTemplate.exchange(
                createGetCommonFriendsUrl(1, 2),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();

        common = testRestTemplate.getForObject(createGetUserByIdUrl(3), User.class);
        assertEquals(1, requestedUsers.size());
        assertTrue(requestedUsers.contains(common));

    }

    @Test
    public void shouldReturn400IfUserIdIsZeroWhenGetCommonFriends() {
        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetCommonFriendsUrl(0, 1),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfUserIdIsNegativeWhenGetCommonFriends() {
        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetCommonFriendsUrl(-1, 1),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfComparedUserIdIsZeroWhenGetCommonFriends() {
        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetCommonFriendsUrl(0, 1),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfComparedUserIdIsNegativeWhenGetCommonFriends() {
        ResponseEntity<String> responseEntity =
                testRestTemplate.exchange(
                        createGetCommonFriendsUrl(-1, 1),
                        HttpMethod.GET,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturnEmptyListIfNoCommonFriends() {
        User currentUser = new User(
                "user",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, currentUser, User.class);
        User friend = new User(
                "friend",
                "friend",
                "friend@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, friend, User.class);

        List<User> requestedUsers = testRestTemplate.exchange(
                createGetCommonFriendsUrl(1, 2),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {
                }
        ).getBody();

        assertTrue(requestedUsers.isEmpty());

    }

}
