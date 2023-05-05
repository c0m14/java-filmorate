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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;
import ru.yandex.practicum.filmorate.repository.film.h2.RatingMpaDao;
import ru.yandex.practicum.filmorate.util.TestDataProducer;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilmTest {

    private static final String HOST = "http://localhost:";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    HttpHeaders applicationJsonHeaders;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    @Qualifier("H2FilmRepository")
    private FilmStorage filmStorage;
    @Autowired
    private RatingMpaDao ratingMpaDao;
    @Autowired
    private TestDataProducer testDataProducer;
    @Value(value = "${local.server.port}")
    private int port;
    private URI filmsUrl;

    @BeforeEach
    public void beforeEach() {
        filmsUrl = URI.create(
                HOST +
                        port +
                        "/films"
        );
        applicationJsonHeaders = new HttpHeaders();
        applicationJsonHeaders.setContentType(MediaType.APPLICATION_JSON);

    }

    private URI createGetFilmByIdUrl(Long filmId) {
        return URI.create(
                String.format("%s%s/films/%d", HOST, port, filmId)
        );
    }

    private URI createGiveOrDeleteLikeUrl(Long filmId, Long userId) {
        return URI.create(
                String.format("%s%s/films/%d/like/%d", HOST, port, filmId, userId)
        );
    }

    private URI createGetPopularFilmsWithParameter(int count) {
        return URI.create(
                String.format("%s%s/films/popular?count=%d", HOST, port, count)
        );
    }

    private URI createGetPopularFilmsNoParameter() {
        return URI.create(
                String.format("%s%s/films/popular", HOST, port)
        );
    }

    private URI createGetAllGenres() {
        return URI.create(
                String.format("%s%s/genres", HOST, port)
        );
    }

    private URI createGetGenreById(int id) {
        return URI.create(
                String.format("%s%s/genres/%d", HOST, port, id)
        );
    }

    private URI createGetAllMpa() {
        return URI.create(
                String.format("%s%s/mpa", HOST, port)
        );
    }

    private URI createGetMpaById(int id) {
        return URI.create(
                String.format("%s%s/mpa/%d", HOST, port, id)
        );
    }


    // =============================== POST /films ======================================

    @Test
    public void shouldCreateFilm() {
        Film initialFilm = testDataProducer.getFilmWithoutGenres();

        Film createdFilm = testRestTemplate.postForObject(filmsUrl, initialFilm, Film.class);
        Film savedFilm = filmStorage.getFilmByIdFull(createdFilm.getId()).get();

        assertNotNull(savedFilm, "Film is not saved in database");
        assertEquals(initialFilm.getName(), savedFilm.getName(), "Incorrect field name in saved film");
        assertEquals(initialFilm.getDescription(), savedFilm.getDescription(), "Incorrect field name in saved film");
        assertEquals(initialFilm.getReleaseDate(), savedFilm.getReleaseDate(), "Incorrect field name in saved film");
        assertEquals(initialFilm.getDuration(), savedFilm.getDuration(), "Incorrect field name in saved film");
        assertEquals(initialFilm.getMpa().getId(), savedFilm.getMpa().getId(), "Incorrect field name in saved film");
    }

    @Test
    public void shouldCreateFilmIfMpaIsSentWithoutName() {
        String body = "{" +
                "\"name\": \"New film\"," +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1900-03-25\"," +
                "\"duration\": 200," +
                "\"mpa\": { \"id\": 3}" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        Film createdFilm = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                Film.class).getBody(
        );

        Film savedFilm = filmStorage.getFilmByIdFull(createdFilm.getId()).get();
        assertEquals(savedFilm.getMpa(), ratingMpaDao.getMpaByIdFromDb(3), "Mpa is wrong in saved film");
    }

    @Test
    public void shouldCreateFilmWithGenres() {
        Film initialFilm = testDataProducer.getFilmWithGenres();
        Set<Genre> genres = testDataProducer.getCorrectGenres();

        Film createdFilm = testRestTemplate.postForObject(filmsUrl, initialFilm, Film.class);
        Film savedFilm = filmStorage.getFilmByIdFull(createdFilm.getId()).get();

        assertEquals(genres.size(), savedFilm.getGenres().size(), "Wrong genres count");
        assertEquals(genres, savedFilm.getGenres(), "Genres is not equals in request and database");
    }

    @Test
    public void shouldCreateFilmWithGenresIfOnlyGenreIdIsSent() {
        String body = "{" +
                "\"name\": \"New film\"," +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1900-03-25\"," +
                "\"duration\": 200," +
                "\"mpa\": { \"id\": 3}," +
                "\"genres\": [{ \"id\": 2}, { \"id\": 6}]" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);
        Set<Genre> genres = testDataProducer.getCorrectGenres();

        Film createdFilm = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                Film.class).getBody(
        );

        Film savedFilm = filmStorage.getFilmByIdFull(createdFilm.getId()).get();
        assertTrue(savedFilm.getGenres().equals(genres), "Expected genres is missing in saved film");
        assertEquals(2, savedFilm.getGenres().size(), "Wrong count of genres");
    }

    @Test
    public void shouldCreateFilmIfDuplicateGenres() {
        String body = "{" +
                "\"name\": \"New film\"," +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1900-03-25\"," +
                "\"duration\": 200," +
                "\"mpa\": { \"id\": 3}," +
                "\"genres\": [{ \"id\": 2}, { \"id\": 6}, { \"id\": 2}]" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        Film createdFilm = testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.POST,
                entity,
                Film.class).getBody(
        );

        Film savedFilm = filmStorage.getFilmByIdFull(createdFilm.getId()).get();
        assertEquals(2, savedFilm.getGenres().size(), "Wrong count of genres");
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
    public void shouldReturn404IfMpaRatingIdIsWrong() {
        Film initialFilm = testDataProducer.getFilmWithWrongMpaId();

        ResponseEntity<String> response = testRestTemplate.postForEntity(filmsUrl, initialFilm, String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfGenreIdIsWrong() {
        Film initialFilm = testDataProducer.getFilmWithWrongGenres();

        ResponseEntity<String> response = testRestTemplate.postForEntity(filmsUrl, initialFilm, String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfIdIsSentInRequestWhenFilmCreating() {
        Film film = testDataProducer.getMutableFilm();
        film.setId(1L);

        ResponseEntity<String> entity = testRestTemplate.postForEntity(filmsUrl, film, String.class);

        assertEquals(HttpStatus.valueOf(400), entity.getStatusCode(), "Wrong status code");
    }

    // =============================== PUT /films ======================================

    @Test
    public void shouldUpdateFilm() {
        Film initialFilm = testDataProducer.getMutableFilm();
        Long createdFilmId = filmStorage.addFilm(initialFilm).getId();
        initialFilm.setDescription("New description");
        initialFilm.setId(createdFilmId);
        HttpEntity<Film> entity = new HttpEntity<>(initialFilm, applicationJsonHeaders);

        testRestTemplate.exchange(filmsUrl, HttpMethod.PUT, entity, Film.class);
        Film updatedFilm = filmStorage.getFilmByIdFull(createdFilmId).get();

        assertNotNull(updatedFilm, "Error while saving updated film");
        assertEquals(initialFilm.getName(), updatedFilm.getName(), "Field is not updated");
        assertEquals(initialFilm.getDescription(), updatedFilm.getDescription(), "Field is not updated");
        assertEquals(initialFilm.getReleaseDate(), updatedFilm.getReleaseDate(), "Field is not updated");
        assertEquals(initialFilm.getDuration(), updatedFilm.getDuration(), "Field is not updated");
        assertEquals(initialFilm.getMpa().getId(), updatedFilm.getMpa().getId(), "Field is not updated");
    }

    @Test
    public void shouldUpdateFilmIfMpaIsSentWithoutName() {
        Film initialFilm = testDataProducer.getFilmWithoutGenres();
        Long createdFilmId = filmStorage.addFilm(initialFilm).getId();
        String body = "{" +
                "\"id\": " + createdFilmId + "," +
                "\"name\": \"New film\"," +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1900-03-25\"," +
                "\"duration\": 200," +
                "\"mpa\": { \"id\": 3}" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);
        RatingMPA mpa = ratingMpaDao.getMpaByIdFromDb(3);

        testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.PUT,
                entity,
                Film.class);

        Film savedFilm = filmStorage.getFilmByIdFull(createdFilmId).get();
        assertEquals(savedFilm.getMpa(), mpa, "Error while creating mpa by id");
    }

    @Test
    public void shouldReplaceGenresWhenFilmUpdating() {
        Film initialFilm = testDataProducer.getFilmWithoutGenres();
        Long createdFilmId = filmStorage.addFilm(initialFilm).getId();
        Set<Genre> genres = testDataProducer.getCorrectGenres();
        initialFilm.setGenres(genres);
        HttpEntity<Film> entity = new HttpEntity<>(initialFilm, applicationJsonHeaders);

        testRestTemplate.exchange(filmsUrl, HttpMethod.PUT, entity, Film.class);
        Film savedUpdatedFilm = filmStorage.getFilmByIdFull(createdFilmId).get();

        assertEquals(2, savedUpdatedFilm.getGenres().size(), "Wrong count of genres");
        assertTrue(savedUpdatedFilm.getGenres().containsAll(genres), "Genres are not updated");
    }

    @Test
    public void shouldRemoveGenresIfEmptyGenresListWhenFilmUpdating() {
        Film initialFilm = testDataProducer.getFilmWithGenres();
        Long createdFilmId = filmStorage.addFilm(initialFilm).getId();
        initialFilm.setGenres(Collections.EMPTY_SET);
        HttpEntity<Film> entity = new HttpEntity<>(initialFilm, applicationJsonHeaders);

        testRestTemplate.exchange(filmsUrl, HttpMethod.PUT, entity, Film.class);
        Film savedUpdatedFilm = filmStorage.getFilmByIdFull(createdFilmId).get();

        assertTrue(savedUpdatedFilm.getGenres().isEmpty(), "Genres are not updated");
    }

    @Test
    public void shouldUpdateFilmWithGenresIfOnlyGenreIdIsSent() {
        Film initialFilm = testDataProducer.getFilmWithoutGenres();
        Long createdFilmId = filmStorage.addFilm(initialFilm).getId();
        String body = "{" +
                "\"id\": " + createdFilmId + "," +
                "\"name\": \"New film\"," +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1900-03-25\"," +
                "\"duration\": 200," +
                "\"mpa\": { \"id\": 3}," +
                "\"genres\": [{ \"id\": 2}, { \"id\": 6}]" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);
        Set<Genre> genres = testDataProducer.getCorrectGenres();

        testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.PUT,
                entity,
                Film.class);

        Film savedFilm = filmStorage.getFilmByIdFull(createdFilmId).get();
        assertEquals(savedFilm.getGenres(), genres, "Expected genres are missing in updated film");
        assertEquals(2, savedFilm.getGenres().size(), "Wrong count of genres");
    }

    @Test
    public void shouldUpdateFilmIfDuplicateGenres() {
        Film initialFilm = testDataProducer.getFilmWithoutGenres();
        Long createdFilmId = filmStorage.addFilm(initialFilm).getId();
        String body = "{" +
                "\"id\": " + createdFilmId + "," +
                "\"name\": \"New film\"," +
                "\"description\": \"Description\"," +
                "\"releaseDate\": \"1900-03-25\"," +
                "\"duration\": 200," +
                "\"mpa\": { \"id\": 3}," +
                "\"genres\": [{ \"id\": 2}, { \"id\": 6}, { \"id\": 2}]" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(body, applicationJsonHeaders);

        testRestTemplate.exchange(
                filmsUrl,
                HttpMethod.PUT,
                entity,
                Film.class);

        Film savedFilm = filmStorage.getFilmByIdFull(createdFilmId).get();
        assertEquals(2, savedFilm.getGenres().size(), "Wrong count of genres");
    }


    @Test
    public void shouldReturn400IfNameIsAbsentWhenFilmUpdating() {
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
    public void shouldReturn404IfMpaRatingIdIsWrongWhenFilmUpdating() {
        Film initialFilm = testDataProducer.getMutableFilm();
        Long createdFilmId = filmStorage.addFilm(initialFilm).getId();
        initialFilm.setMpa(new RatingMPA(999, "G"));
        initialFilm.setId(createdFilmId);
        HttpEntity<Film> entity = new HttpEntity<>(initialFilm, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(filmsUrl, HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfGenreIdIsWrongWhenFilmUpdating() {
        Film initialFilm = testDataProducer.getMutableFilm();
        Long createdFilmId = filmStorage.addFilm(initialFilm).getId();
        initialFilm.setGenres(testDataProducer.getWrongGenres());
        initialFilm.setId(createdFilmId);
        HttpEntity<Film> entity = new HttpEntity<>(initialFilm, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(filmsUrl, HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfIdIsAbsentInRequestWhenFilmUpdating() {
        Film updated = testDataProducer.getMutableFilm();
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
    public void shouldReturn404IfIdIsWrongInRequestWhenFilmUpdating() {
        Film initialFilm = testDataProducer.getMutableFilm();
        initialFilm.setId(999L);
        HttpEntity<Film> entity = new HttpEntity<>(initialFilm, applicationJsonHeaders);

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
        Film film = testDataProducer.getMutableFilm();
        Long createdFilmId = filmStorage.addFilm(film).getId();
        film.setId(createdFilmId);

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
    public void shouldReturnFilmById() {
        Film film = testDataProducer.getMutableFilm();
        Long createdFilmId = filmStorage.addFilm(film).getId();
        film.setId(createdFilmId);

        Film requestedFilm = testRestTemplate.exchange(
                createGetFilmByIdUrl(createdFilmId),
                HttpMethod.GET,
                null,
                Film.class
        ).getBody();

        assertEquals(film, requestedFilm, "Wrong film returned");
    }

    @Test
    public void shouldReturn400IfIdIsZero() {

        ResponseEntity<Film> responseEntity = testRestTemplate.exchange(
                createGetFilmByIdUrl(0L),
                HttpMethod.GET,
                null,
                Film.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfIdIsNegative() {

        ResponseEntity<Film> responseEntity = testRestTemplate.exchange(
                createGetFilmByIdUrl(-1L),
                HttpMethod.GET,
                null,
                Film.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfFilmNotFoundById() {

        ResponseEntity<Film> responseEntity = testRestTemplate.exchange(
                createGetFilmByIdUrl(9999L),
                HttpMethod.GET,
                null,
                Film.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    // =============================== PUT /films/{id}/like/{userId} ======================================

    @Test
    public void shouldGiveLikeFromUserToFilm() {
        Long userId = testDataProducer.addDefaultUserToDB();
        Long usualFilmId = testDataProducer.addDefaultFilmToDB();
        Long filmToBeLikedId = testDataProducer.addDefaultFilmToDB();

        testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(filmToBeLikedId, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        Film toBeLikedFilm = filmStorage.getFilmByIdFull(filmToBeLikedId).get();
        Film usualFilm = filmStorage.getFilmByIdFull(usualFilmId).get();
        assertEquals(1, toBeLikedFilm.getLikesCount(), "Wrong likes count");
        assertEquals(0, usualFilm.getLikesCount(), "Wrong likes count");
    }

    @Test
    public void shouldReturn404IfFilmIdIsZeroWhenGivingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(0L, 1L),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfFilmIdIsNegativeWhenGivingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(-1L, 1L),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsZeroWhenGivingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1L, 0L),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenGivingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1L, -1L),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfFilmIsAbsentWhenGivingLike() {
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(9999L, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIsAbsentWhenGivingLike() {
        Long filmId = testDataProducer.addDefaultFilmToDB();

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(filmId, 9999L),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    // =============================== DELETE /films/{id}/like/{userId} ======================================

    @Test
    public void shouldRemoveUserLikeFromFilm() {
        Long toBeLikedFilmId = testDataProducer.addDefaultFilmToDB();
        Long userId = testDataProducer.addDefaultUserToDB();
        filmStorage.giveLikeFromUserToFilm(toBeLikedFilmId, userId);

        testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(toBeLikedFilmId, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        Film toBeLikedFilm = filmStorage.getFilmByIdFull(toBeLikedFilmId).get();
        assertEquals(0, toBeLikedFilm.getLikesCount(), "Like has not been deleted");
    }

    @Test
    public void shouldReturn404IfFilmIdIsZeroWhenRemovingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(0L, 1L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfFilmIdIsNegativeWhenRemovingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(-1L, 1L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsZeroWhenRemovingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1L, 0L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenRemovingLike() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(1L, -1L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfFilmIsAbsentWhenRemovingLike() {
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(9999L, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIsAbsentWhenRemovingLike() {
        Long filmId = testDataProducer.addDefaultFilmToDB();

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGiveOrDeleteLikeUrl(filmId, 9999L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    // =============================== GET /films/popular ======================================

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturnPopularFilmsWithoutCountParameter() {
        testDataProducer.createContextWithPopularFilms();

        List<Film> requestedFilms = testRestTemplate.exchange(
                createGetPopularFilmsNoParameter(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Film>>() {
                }
        ).getBody();

        Film mostPopularFilm = filmStorage.getFilmByIdFull(1L).get();
        Film leastPopularFilm = filmStorage.getFilmByIdFull(10L).get();
        assertEquals(10, requestedFilms.size(), "Wrong popular films list size");
        assertEquals(mostPopularFilm, requestedFilms.get(0), "Wrong most popular film in response");
        assertEquals(leastPopularFilm, requestedFilms.get(9), "Wrong least popular film in response");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturnPopularFilmsWithCountParameter() {
        testDataProducer.createContextWithPopularFilms();

        List<Film> requestedFilms = testRestTemplate.exchange(
                createGetPopularFilmsWithParameter(11),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Film>>() {
                }
        ).getBody();

        Film mostPopularFilm = filmStorage.getFilmByIdFull(1L).get();
        Film leastPopularFilm = filmStorage.getFilmByIdFull(11L).get();
        assertEquals(11, requestedFilms.size(), "Wrong popular films list size");
        assertEquals(mostPopularFilm, requestedFilms.get(0), "Wrong most popular film in response");
        assertEquals(leastPopularFilm, requestedFilms.get(10), "Wrong least popular film in response");
    }

    @Test
    public void shouldReturn400IfCountIsZeroWhenGetPopularFilms() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGetPopularFilmsWithParameter(0),
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfCountIsNegativeWhenGetPopularFilms() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGetPopularFilmsWithParameter(-1),
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

// =============================== GET /genres ======================================

    @Test
    public void shouldReturnAllGenresList() {
        List<Genre> fullGenresList = List.of(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма"),
                new Genre(3, "Мультфильм"),
                new Genre(4, "Триллер"),
                new Genre(5, "Документальный"),
                new Genre(6, "Боевик")
        );

        List<Genre> requestedGenres = testRestTemplate.exchange(
                createGetAllGenres(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Genre>>() {
                }
        ).getBody();

        assertEquals(6, requestedGenres.size(), "Wrong genres number in response");
        assertEquals(fullGenresList, requestedGenres, "Genres don't fetch");
    }

// =============================== GET /genres/{id} ======================================

    @Test
    public void shouldReturnGenreById() {

        Genre requestedGenre = testRestTemplate.getForObject(
                createGetGenreById(1),
                Genre.class
        );

        assertEquals("Комедия", requestedGenre.getName(), "Wrong genre in response");
        assertEquals(1, requestedGenre.getId(), "Wrong genre in response");
    }

    @Test
    public void shouldReturn400IfGenreIdIsZero() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGetGenreById(0),
                HttpMethod.GET,
                null,
                String.class);

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfGenreIdIsNegative() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGetGenreById(-1),
                HttpMethod.GET,
                null,
                String.class);

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfGenreIdIsNotExist() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGetGenreById(999),
                HttpMethod.GET,
                null,
                String.class);

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

    // =============================== GET /mpa ======================================

    @Test
    public void shouldGetAllMpaRatings() {
        List<RatingMPA> fullRatingMpaList = List.of(
                new RatingMPA(1, "G"),
                new RatingMPA(2, "PG"),
                new RatingMPA(3, "PG-13"),
                new RatingMPA(4, "R"),
                new RatingMPA(5, "NC-17")
        );

        List<RatingMPA> requestedMpa = testRestTemplate.exchange(
                createGetAllMpa(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RatingMPA>>() {
                }
        ).getBody();

        assertEquals(5, requestedMpa.size(), "Wrong mpa number in response");
        assertEquals(fullRatingMpaList, requestedMpa, "Mpa don't fetch");
    }

    // =============================== GET /mpa/{id} ======================================
    @Test
    public void shouldReturnMpaById() {

        RatingMPA requestedMpa = testRestTemplate.getForObject(
                createGetMpaById(1),
                RatingMPA.class
        );

        assertEquals("G", requestedMpa.getName(), "Wrong mpa in response");
        assertEquals(1, requestedMpa.getId(), "Wrong mpa in response");
    }

    @Test
    public void shouldReturn400IfMpaIdIsZero() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGetMpaById(0),
                HttpMethod.GET,
                null,
                String.class);

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfMpaIdIsNegative() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGetMpaById(-1),
                HttpMethod.GET,
                null,
                String.class);

        assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfMpaIdIsNotExist() {

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createGetMpaById(999),
                HttpMethod.GET,
                null,
                String.class);

        assertEquals(HttpStatus.valueOf(404), responseEntity.getStatusCode(), "Wrong status code");
    }

}
