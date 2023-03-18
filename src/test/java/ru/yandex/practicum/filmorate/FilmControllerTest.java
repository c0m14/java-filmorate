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

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilmControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Value(value = "${local.server.port}")
    private int PORT;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String HOST = "http://localhost:";
    private URI filmsUrl;
    HttpHeaders applicationJsonHeaders;


    @BeforeEach
    public void beforeEach() {
        filmsUrl = URI.create(
                new StringBuilder()
                        .append(HOST)
                        .append(PORT)
                        .append("/films")
                        .toString()
        );
        applicationJsonHeaders = new HttpHeaders();
        applicationJsonHeaders.setContentType(MediaType.APPLICATION_JSON);

    }

    //post tests
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

        film.setId(1);
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
    public void shouldReturn500IfNameIsAbsentWhenFilmCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"description\": \"Description\",")
                .append("\"releaseDate\": \"1900-03-25\",")
                .append("\"duration\": 200")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn500IfNameIsEmptyWhenFilmCreating() {
        Film film = new Film(
                "",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );

        ResponseEntity<String> entity = testRestTemplate.postForEntity(filmsUrl, film, String.class);

        assertEquals(HttpStatusCode.valueOf(500), entity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn500IfDescriptionIsAbsentWhenFilmCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"Titanic\",")
                .append("\"releaseDate\": \"1900-03-25\",")
                .append("\"duration\": 200")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn500IfDescriptionIsLongerThan200WhenFilmCreating() {
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

        assertEquals(HttpStatusCode.valueOf(500), entity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn500IfRealiseDateIsAbsentWhenFilmCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"Titanic\",")
                .append("\"description\": \"Description\",")
                .append("\"duration\": 200")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn500IfRealiseDateIsEmptyWhenFilmCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"Titanic\",")
                .append("\"description\": \"Description\",")
                .append("\"duration\": 200,")
                .append("\"releaseDate\": \" \" ")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn500IfReleaseDateIsBefore28d12m1895yWhenFilmCreating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1850-01-01", formatter),
                120
        );

        ResponseEntity<String> entity = testRestTemplate.postForEntity(filmsUrl, film, String.class);

        assertEquals(HttpStatusCode.valueOf(500), entity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn500IfDurationIsAbsentWhenFilmCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"Titanic\",")
                .append("\"description\": \"Description\",")
                .append("\"releaseDate\": \"1994-01-01\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn500IfDurationIsEmptyWhenFilmCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"Titanic\",")
                .append("\"description\": \"Description\",")
                .append("\"releaseDate\": \"1994-01-01\",")
                .append("\"duration\": \" \" ")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn500IfDurationIsZeroWhenFilmCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"Titanic\",")
                .append("\"description\": \"Description\",")
                .append("\"releaseDate\": \"1994-01-01\",")
                .append("\"duration\": 0")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn500IfDurationIsNegativeWhenFilmCreating() {
        String body = new StringBuilder()
                .append("{")
                .append("\"name\": \"Titanic\",")
                .append("\"description\": \"Description\",")
                .append("\"releaseDate\": \"1994-01-01\",")
                .append("\"duration\": -10")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn500IfIdIsSentInPostRequestWhenFilmCreating() {
        Film film = new Film(
                1,
                "Titanic",
                "Drama",
                LocalDate.parse("1850-01-01", formatter),
                120
        );

        ResponseEntity<String> entity = testRestTemplate.postForEntity(filmsUrl, film, String.class);

        assertEquals(HttpStatusCode.valueOf(500), entity.getStatusCode(), "Wrong status code");
    }

    //put tests
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
        film.setId(1);
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
        film.setId(1);
        HttpEntity<Film> entity = new HttpEntity<>(film, applicationJsonHeaders);

        Film updatedFilm = testRestTemplate.exchange(filmsUrl, HttpMethod.PUT, entity, Film.class)
                .getBody();

        assertEquals(createdFilm.getId(), updatedFilm.getId(), "Id has changed");
    }

    @Test
    public void shouldReturn500IfNameIsAbsentWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"description\": \"Description\",")
                .append("\"releaseDate\": \"1994-01-01\",")
                .append("\"duration\": 100")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfNameIsEmptyWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updated = new Film(
                1,
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

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfDescriptionIsAbsentWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"Titanic\",")
                .append("\"releaseDate\": \"1994-01-01\",")
                .append("\"duration\": 100")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfDescriptionIsLongerThan200WhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updatedFilm = new Film(
                1,
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

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfRealiseDateIsAbsentWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"Titanic\",")
                .append("\"description\": \"Drama\",")
                .append("\"duration\": 100")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfRealiseDateIsEmptyWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"Titanic\",")
                .append("\"releaseDate\": \" \",")
                .append("\"description\": \"Drama\",")
                .append("\"duration\": 100")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfReleaseDateIsBefore28d12m1895yWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updated = new Film(
                1,
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

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfDurationIsAbsentWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"Titanic\",")
                .append("\"releaseDate\": \"1994-01-01\",")
                .append("\"description\": \"Drama\"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code");
    }

    @Test
    public void shouldReturn500IfDurationIsEmptyWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        String body = new StringBuilder()
                .append("{")
                .append("\"id\": 1,")
                .append("\"name\": \"Titanic\",")
                .append("\"releaseDate\": \"1994-01-01\",")
                .append("\"description\": \"Drama\",")
                .append("\"duration\": \" \"")
                .append("}")
                .toString();
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        ResponseEntity<Film> filmResponseEntity = testRestTemplate.exchange(filmsUrl,
                HttpMethod.PUT,
                entity, Film.class
        );

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfDurationIsZeroWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updated = new Film(
                1,
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

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfDurationIsNegativeWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updated = new Film(
                1,
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

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfIdIsAbsentInPostRequestWhenFilmUpdating() {
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

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    @Test
    public void shouldReturn500IfIdIsWrongInPostRequestWhenFilmUpdating() {
        Film film = new Film(
                "Titanic",
                "Drama",
                LocalDate.parse("1994-01-01", formatter),
                120
        );
        testRestTemplate.postForObject(filmsUrl, film, Film.class);
        Film updated = new Film(
                3,
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

        assertEquals(HttpStatusCode.valueOf(500),
                filmResponseEntity.getStatusCode(),
                "Wrong status code"
        );
    }

    //get tests
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
        film.setId(1);

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

}
