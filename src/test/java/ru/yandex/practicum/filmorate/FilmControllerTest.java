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
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilmControllerTest {

    private static final String HOST = "http://localhost:";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    HttpHeaders applicationJsonHeaders;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Value(value = "${local.server.port}")
    private int PORT;
    private URI filmsUrl;

    @BeforeEach
    public void beforeEach() {
        filmsUrl = URI.create(
                HOST +
                        PORT +
                        "/films"
        );
        applicationJsonHeaders = new HttpHeaders();
        applicationJsonHeaders.setContentType(MediaType.APPLICATION_JSON);

    }

    private URI createGetFilmByIdUrl(int filmId) {
        return URI.create(
                String.format("%s%s/films/%d", HOST, PORT, filmId)
        );
    }

    private URI createGiveOrDeleteLikeUrl(int filmId, int userId) {
        return URI.create(
                String.format("%s%s/films/%d/like/%d", HOST, PORT, filmId, userId)
        );
    }

    private URI createGetPopularFilmsWithParameter(int count) {
        return URI.create(
                String.format("%s%s/films/popular?count=%d", HOST, PORT, count)
        );
    }

    private URI createGetPopularFilmsNoParameter() {
        return URI.create(
                String.format("%s%s/films/popular", HOST, PORT)
        );
    }

    private void createContextWithPopularFilms() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        for (int i = 0; i <= 10; i++) {
            testRestTemplate.postForObject(URI.create(String.format("%s%s/users", HOST, PORT)),
                    user,
                    User.class);
            testRestTemplate.postForObject(filmsUrl, film, Film.class);
        }

        int likesCount = 11;
        for (int i = 1; i <= 11; i++) {
            for (int j = likesCount; j >= 1; j--) {
                testRestTemplate.exchange(
                        createGiveOrDeleteLikeUrl(i, j),
                        HttpMethod.PUT,
                        null,
                        String.class
                );
            }
            likesCount--;
        }
    }

    // =============================== POST /films ======================================

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldCreateFilm() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );

        Film createdFilm = testRestTemplate.postForObject(filmsUrl, film, Film.class);

        film.setId(1L);
        assertEquals(film, createdFilm, "Film creation Error");
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldIncrementIdCounterWhenFilmCreating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );

        Film createdFilm = testRestTemplate.postForObject(filmsUrl, film, Film.class);

        assertEquals(1, createdFilm.getId(), "Id is not correct");
    }

    @Test
    public void shouldReturn400IfNameIsAbsentWhenFilmCreating() {
        String body = "{" +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1900-03-25\"," +
                "\"duration\": 200" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");

    }

    @Test
    public void shouldReturn400IfNameIsEmptyWhenFilmCreating() {
        Film film = new Film(
                "",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );

        ResponseEntity<String> entity = testRestTemplate.postForEntity(filmsUrl, film, String.class);

        assertEquals(HttpStatus.valueOf(400), entity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfDescriptionIsAbsentWhenFilmCreating() {
        String body = "{" +
                "\"name\": \"Titanic\"," +
                "\"releaseDate\": \"1900-03-25\"," +
                "\"duration\": 200" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfDescriptionIsLongerThan200WhenFilmCreating() {
        Film film = new Film(
                "Titanic",
                "Titanic is a 1997 American epic romance and disaster film directed, written, produced," +
                        " and co-edited by James Cameron. Incorporating both historical and fictionalized aspects," +
                        " it is based on accounts of the sinking of the RMS Titanic and stars Kate Winslet" +
                        " and Leonardo DiCaprio as members of different social classes who fall in love aboard" +
                        " the ship during its ill-fated maiden voyage. The film also features Billy Zane," +
                        " Kathy Bates, Frances Fisher, Gloria Stuart, Bernard Hill, Jonathan Hyde, Victor Garber," +
                        " and Bill Paxton.",
                LocalDate.parse("1994-01-01", formatter),
                120
        );

        ResponseEntity<String> entity = testRestTemplate.postForEntity(filmsUrl, film, String.class);

        assertEquals(HttpStatus.valueOf(400), entity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfRealiseDateIsAbsentWhenFilmCreating() {
        String body = "{" +
                "\"name\": \"Titanic\"," +
                "\"description\": \"Description\"," +
                "\"duration\": 200" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfRealiseDateIsEmptyWhenFilmCreating() {
        String body = "{" +
                "\"name\": \"Titanic\"," +
                "\"description\": \"Description\"," +
                "\"duration\": 200," +
                "\"releaseDate\": \" \" " +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfReleaseDateIsBefore28d12m1895yWhenFilmCreating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1850-01-01", formatter),
                120
        );

        ResponseEntity<String> entity = testRestTemplate.postForEntity(filmsUrl, film, String.class);

        assertEquals(HttpStatus.valueOf(400), entity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfDurationIsAbsentWhenFilmCreating() {
        String body = "{" +
                "\"name\": \"Titanic\"," +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1994-01-01\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfDurationIsEmptyWhenFilmCreating() {
        String body = "{" +
                "\"name\": \"Titanic\"," +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1994-01-01\"," +
                "\"duration\": \" \" " +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfDurationIsZeroWhenFilmCreating() {
        String body = "{" +
                "\"name\": \"Titanic\"," +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1994-01-01\"," +
                "\"duration\": 0" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfDurationIsNegativeWhenFilmCreating() {
        String body = "{" +
                "\"name\": \"Titanic\"," +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1994-01-01\"," +
                "\"duration\": -10" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfIdIsSentInRequestWhenFilmCreating() {
        Film film = new Film(
                1L,
                "Titanic",
                "Drama",
                LocalDate.parse("1850-01-01", formatter),
                120
        );

        ResponseEntity<String> entity = testRestTemplate.postForEntity(filmsUrl, film, String.class);

        assertEquals(HttpStatus.valueOf(400), entity.getStatusCode(), "Wrong status code");
    }

    // =============================== PUT /films ======================================

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldUpdateFilm() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        film.setId(1L);
        film.setDescription("New description");
        HttpEntity<Film> entity = new HttpEntity<>(film, applicationJsonHeaders);


        Film updatedFilm = testRestTemplate.exchange(filmsUrl, HttpMethod.PUT, entity, Film.class)
                .getBody();

        assertEquals(film, updatedFilm, "Updated film is not correct");
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldNotIncrementIdCounterWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        Film createdFilm = testRestTemplate.postForObject(filmsUrl, film, Film.class);
        film.setDescription("New description");
        film.setId(1L);
        HttpEntity<Film> entity = new HttpEntity<>(film, applicationJsonHeaders);

        Film updatedFilm = testRestTemplate.exchange(filmsUrl, HttpMethod.PUT, entity, Film.class)
                .getBody();

        assertEquals(createdFilm.getId(), updatedFilm.getId(), "Id has changed");
    }

    @Test
    public void shouldReturn400IfNameIsAbsentWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = "{" +
                "\"id\": 1," +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1994-01-01\"," +
                "\"duration\": 100" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfNameIsEmptyWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updated = new Film(
                1L,
                "",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        HttpEntity<Film> entity = new HttpEntity<>(updated, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfDescriptionIsAbsentWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"Titanic\"," +
                "\"releaseDate\": \"1994-01-01\"," +
                "\"duration\": 100" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfDescriptionIsLongerThan200WhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updatedFilm = new Film(
                1L,
                "Titanic",
                "Titanic is a 1997 American epic romance and disaster film directed, written, produced," +
                        " and co-edited by James Cameron. Incorporating both historical and fictionalized aspects," +
                        " it is based on accounts of the sinking of the RMS Titanic and stars Kate Winslet" +
                        " and Leonardo DiCaprio as members of different social classes who fall in love aboard" +
                        " the ship during its ill-fated maiden voyage. The film also features Billy Zane," +
                        " Kathy Bates, Frances Fisher, Gloria Stuart, Bernard Hill, Jonathan Hyde, Victor Garber," +
                        " and Bill Paxton.",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        HttpEntity<Film> entity = new HttpEntity<>(updatedFilm, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfRealiseDateIsAbsentWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"Titanic\"," +
                "\"description\": \"Drama\"," +
                "\"duration\": 100" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfRealiseDateIsEmptyWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"Titanic\"," +
                "\"releaseDate\": \" \"," +
                "\"description\": \"Drama\"," +
                "\"duration\": 100" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfReleaseDateIsBefore28d12m1895yWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updated = new Film(
                1L,
                "Titanic",
                "Drama",
                LocalDate.parse("1801-01-01", formatter),
                120
        );
        HttpEntity<Film> entity = new HttpEntity<>(updated, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfDurationIsAbsentWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"Titanic\"," +
                "\"releaseDate\": \"1994-01-01\"," +
                "\"description\": \"Drama\"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code");
    }

    @Test
    public void shouldReturn400IfDurationIsEmptyWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = "{" +
                "\"id\": 1," +
                "\"name\": \"Titanic\"," +
                "\"releaseDate\": \"1994-01-01\"," +
                "\"description\": \"Drama\"," +
                "\"duration\": \" \"" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfDurationIsZeroWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updated = new Film(
                1L,
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                0
        );
        HttpEntity<Film> entity = new HttpEntity<>(updated, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfDurationIsNegativeWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updated = new Film(
                1L,
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                -10
        );
        HttpEntity<Film> entity = new HttpEntity<>(updated, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn400IfIdIsAbsentInRequestWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updated = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                100
        );
        HttpEntity<Film> entity = new HttpEntity<>(updated, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(400),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfIdIsWrongInRequestWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updated = new Film(
                3L,
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                100
        );
        HttpEntity<Film> entity = new HttpEntity<>(updated, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatus.valueOf(404),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    // =============================== GET /films ======================================

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldReturnFilms() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        film.setId(1L);

        List<Film> requestedFilms = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Film>>() {
                }
        ).getBody();

        assertEquals(1, requestedFilms.size(), "Wrong number of returned elements");
        assertTrue(requestedFilms.contains(film), "Expected element is not in List");
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void shouldReturnEmptyListIfNoFilmsCreated() {
        List<Film> requestedFilms = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Film>>() {
                }
        ).getBody();

        assertEquals(0, requestedFilms.size(), "Wrong number of returned elements");
    }

    // =============================== GET /films/{id} ======================================

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturnFilmById() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        film.setId(1L);

        Film requestedFilm = testRestTemplate.exchange(
                createGetFilmByIdUrl(1),
                HttpMethod.GET,
                null,
                Film.class
        ).getBody();

        assertEquals(film, requestedFilm);
    }

    @Test
    public void shouldReturn400IfIdIsZero() {

        ResponseEntity<Film> responseEntity = testRestTemplate.exchange(
                createGetFilmByIdUrl(0),
                HttpMethod.GET,
                null,
                Film.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfIdIsNegative() {

        ResponseEntity<Film> responseEntity = testRestTemplate.exchange(
                createGetFilmByIdUrl(-1),
                HttpMethod.GET,
                null,
                Film.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfFilmNotFoundById() {

        ResponseEntity<Film> responseEntity = testRestTemplate.exchange(
                createGetFilmByIdUrl(1),
                HttpMethod.GET,
                null,
                Film.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode());
    }

    // =============================== PUT /films/{id}/like/{userId} ======================================

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGiveLikeFromUserToFilm() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(URI.create(String.format("%s%s/users", HOST, PORT)),
                user,
                User.class
        );
        user.setId(1L);
        Film usualFilm = new Film(
                "Titanic2",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, usualFilm, Film.class);
        usualFilm.setId(1L);
        Film toBeLikedFilm = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, toBeLikedFilm, Film.class);
        toBeLikedFilm.setId(2L);

        testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(2, 1),
                HttpMethod.PUT,
                null,
                String.class
        );

        List<Film> requestedFilms = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Film>>() {
                }
        ).getBody();
        toBeLikedFilm = requestedFilms.get(1);
        usualFilm = requestedFilms.get(0);
        assertEquals(1, toBeLikedFilm.getLikedFilmIds().size());
        assertTrue(toBeLikedFilm.getLikedFilmIds().contains(user.getId()));
        assertTrue(usualFilm.getLikedFilmIds().isEmpty());
    }

    @Test
    public void shouldReturn400IfFilmIdIsZeroWhenGivingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(0, 1),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfFilmIdIsNegativeWhenGivingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(-1, 1),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfUserIdIsZeroWhenGivingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1, 0),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfUserIdIsNegativeWhenGivingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1, -1),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfFilmIsAbsentWhenGivingLike() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(URI.create(String.format("%s%s/users", HOST, PORT)),
                user,
                User.class
        );

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1, 1),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfUserIsAbsentWhenGivingLike() {
        Film toBeLikedFilm = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, toBeLikedFilm, Film.class);

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1, 1),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode());
    }

    // =============================== DELETE /films/{id}/like/{userId} ======================================

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldRemoveUserLikeFromFilm() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(URI.create(String.format("%s%s/users", HOST, PORT)),
                user,
                User.class
        );
        user.setId(1L);
        Film usualFilm = new Film(
                "Titanic2",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, usualFilm, Film.class);
        usualFilm.setId(1L);
        Film toBeLikedFilm = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, toBeLikedFilm, Film.class);
        toBeLikedFilm.setId(2L);
        testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(2, 1),
                HttpMethod.PUT,
                null,
                String.class
        );

        testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(2, 1),
                HttpMethod.DELETE,
                null,
                String.class
        );

        List<Film> requestedFilms = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Film>>() {
                }
        ).getBody();
        toBeLikedFilm = requestedFilms.get(1);
        assertTrue(toBeLikedFilm.getLikedFilmIds().isEmpty());
    }

    @Test
    public void shouldReturn400IfFilmIdIsZeroWhenRemovingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(0, 1),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfFilmIdIsNegativeWhenRemovingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(-1, 1),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfUserIdIsZeroWhenRemovingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1, 0),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfUserIdIsNegativeWhenRemovingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1, -1),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfFilmIsAbsentWhenRemovingLike() {
        User user = new User(
                "name",
                "login",
                "email@domen.ru",
                LocalDate.of(2000, 1, 1)
        );
        testRestTemplate.postForObject(URI.create(String.format("%s%s/users", HOST, PORT)),
                user,
                User.class
        );

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1, 1),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturn404IfUserIsAbsentWhenRemovingLike() {
        Film toBeLikedFilm = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, toBeLikedFilm, Film.class);

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1, 1),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturnPopularFilmsWithoutCountParameter() {
        createContextWithPopularFilms();

        List<Film> requestedFilms = testRestTemplate.exchange(
                createGetPopularFilmsNoParameter(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Film>>() {
                }
        ).getBody();

        Film mostPopularFilm = requestedFilms.stream()
                .max(Comparator.comparingInt(film -> film.getLikedFilmIds().size()))
                .get();
        Film leastPopularFilm = requestedFilms.stream()
                .min(Comparator.comparingInt(film -> film.getLikedFilmIds().size()))
                .get();
        assertEquals(10, requestedFilms.size());
        assertEquals(mostPopularFilm, requestedFilms.get(0));
        assertEquals(leastPopularFilm, requestedFilms.get(9));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturnPopularFilmsWithCountParameter() {
        createContextWithPopularFilms();

        List<Film> requestedFilms = testRestTemplate.exchange(
                createGetPopularFilmsWithParameter(11),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Film>>() {
                }
        ).getBody();

        Film mostPopularFilm = requestedFilms.stream()
                .max(Comparator.comparingInt(film -> film.getLikedFilmIds().size()))
                .get();
        Film leastPopularFilm = requestedFilms.stream()
                .min(Comparator.comparingInt(film -> film.getLikedFilmIds().size()))
                .get();
        assertEquals(11, requestedFilms.size());
        assertEquals(mostPopularFilm, requestedFilms.get(0));
        assertEquals(leastPopularFilm, requestedFilms.get(10));
    }

    @Test
    public void shouldReturn400IfCountIsZeroWhenGetPopularFilms() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGetPopularFilmsWithParameter(0),
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturn400IfCountIsNegativeWhenGetPopularFilms() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGetPopularFilmsWithParameter(-1),
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
    }

}
