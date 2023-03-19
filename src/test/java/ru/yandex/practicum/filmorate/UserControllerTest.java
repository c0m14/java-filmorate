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
import ru.yandex.practicum.filmorate.model.User;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        usersUrl = URI.create(
                HOST +
                        PORT +
                        "/users"
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
    public void shouldReturn500IfEmailIsAbsentWhenUserCreating() {
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

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfEmailIsEmptyWhenUserCreating() {
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

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfEmailNotContainsSymbolAtWhenUserCreating() {
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

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsAbsentWhenUserCreating() {
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

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsEmptyWhenUserCreating() {
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

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsContainsSpacesOutsideWhenUserCreating() {
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

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfLoginIsContainsSpacesInsideWhenUserCreating() {
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

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfBirthdayIsAbsentWhenUserCreating() {
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

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfBirthdayIsEmptyWhenUserCreating() {
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

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfBirthdayIsLaterWhenNowWhenUserCreating() {
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

        assertEquals(HttpStatusCode.valueOf(500),
                responseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfIdIsSentInPostRequestWhenUserCreating() {
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
    public void shouldReturn500IfEmailIsAbsentWhenUserUpdating() {
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
