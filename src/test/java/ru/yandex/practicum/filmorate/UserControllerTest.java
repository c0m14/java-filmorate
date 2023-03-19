package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Value(value = "${local.server.port}")
    private int PORT;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String HOST = "http://localhost:";
    private URI usersUrl;
    HttpHeaders applicationJsonHeaders;


    @BeforeEach
    public void beforeEach() {
        usersUrl = URI.create(
                new StringBuilder()
                        .append(HOST)
                        .append(PORT)
                        .append("/users")
                        .toString()
        );
        applicationJsonHeaders = new HttpHeaders();
        applicationJsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    //post tests
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

        user.setId(1);
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

        assertEquals(1, createdUser.getId());
    }

    @Test
    public void shouldUseLoginForNameIfNameIsAbsentWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
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
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \" \",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        User createdUser = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class).getBody();

        assertEquals(createdUser.getLogin(), createdUser.getName(), "Name and login is not equal");
    }

    @Test
    public void shouldReturn500IfEmailIsAbsentWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"name\",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfEmailIsEmptyWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"name\",")
                .append("\"email\": \" \",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfEmailNotContainsSymbolAtWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"name\",")
                .append("\"email\": \"email&domen.ru\",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsAbsentWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"name\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsEmptyWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"name\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"login\": \" \",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsContainsSpacesOutsideWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"name\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"login\": \"login \",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsContainsSpacesInsideWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"name\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"login\": \"log in\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfBirthdayIsAbsentWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"name\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"login\": \"login\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfBirthdayIsEmptyWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"name\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"birthday\": \" \",")
                .append("\"login\": \"login\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfBirthdayIsLaterWhenNowWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"name\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2100-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfIdIsSentInPostRequestWhenUserCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"name\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }


    //put tests
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
        user.setId(1);
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
        user.setId(1);
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
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
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
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \" \",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
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
    public void shouldReturn500IfEmailIsAbsentWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"new name\",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfEmailIsEmptyWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"new name\",")
                .append("\"email\": \" \",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfEmailNotContainsSymbolAtWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"new name\",")
                .append("\"email\": \"emaildomen.ru\",")
                .append("\"login\": \"login\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsAbsentWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"new name\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsEmptyWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"new name\",")
                .append("\"login\": \" \",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsContainsSpacesOutsideWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"new name\",")
                .append("\"login\": \"login \",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsContainsSpacesInsideWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"new name\",")
                .append("\"login\": \"lo gin\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfBirthdayIsAbsentWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"new name\",")
                .append("\"login\": \"login\",")
                .append("\"email\": \"email@domen.ru\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfBirthdayIsEmptyWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"new name\",")
                .append("\"login\": \"login\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"birthday\": \" \"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfBirthdayIsLaterWhenNowWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"new name\",")
                .append("\"login\": \"login\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"birthday\": \"2100-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
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
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"new name\",")
                .append("\"login\": \"login\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldReturn500IfIdIsWrongInRequestWhenUserUpdating() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(usersUrl, user, User.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 3,")
                .append("\"name\": \"new name\",")
                .append("\"login\": \"login\",")
                .append("\"email\": \"email@domen.ru\",")
                .append("\"birthday\": \"2000-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<User> responseEntity = testRestTemplate.exchange(
                usersUrl,
                HttpMethod.PUT,
                entity,
                User.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    //get tests
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
        user.setId(1);

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
}
