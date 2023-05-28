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
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.repository.filmReview.FilmReviewStorage;
import ru.yandex.practicum.filmorate.util.FilmReviewTestDataProducer;
import ru.yandex.practicum.filmorate.util.TestDataProducer;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilmReviewTest {
    private static final String HOST = "http://localhost:";
    HttpHeaders applicationJsonHeaders;
    @Value(value = "${local.server.port}")
    private int port;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private FilmReviewTestDataProducer filmReviewTestDataProducer;
    @Autowired
    private TestDataProducer testDataProducer;
    @Autowired
    private FilmReviewStorage filmReviewStorage;

    private URI reviewsUrl;

    @BeforeEach
    public void beforeEach() {
        reviewsUrl = URI.create(
                HOST +
                        port +
                        "/reviews"
        );
        applicationJsonHeaders = new HttpHeaders();
        applicationJsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    private URI getDeleteOrGetByURI(Long reviewId) {
        return URI.create(
                String.format("%s%s/reviews/%d", HOST, port, reviewId)
        );
    }

    private URI getGetReviewsByFilmIdWithCountURI(Long filmId, int count) {
        return URI.create(
                String.format("%s%s/reviews?filmId=%d&count=%d", HOST, port, filmId, count)
        );
    }

    private URI getGetReviewsByFilmIdNoCountURI(Long filmId) {
        return URI.create(
                String.format("%s%s/reviews?filmId=%d", HOST, port, filmId)
        );
    }

    private URI getGetReviewsNoFilmIdWithCountURI(int count) {
        return URI.create(
                String.format("%s%s/reviews?count=%d", HOST, port, count)
        );
    }

    private URI getReviewLikesURI(Long reviewId, Long userId) {
        return URI.create(
                String.format("%s%s/reviews/%d/like/%d", HOST, port, reviewId, userId)
        );
    }

    private URI getReviewDislikesURI(Long reviewId, Long userId) {
        return URI.create(
                String.format("%s%s/reviews/%d/dislike/%d", HOST, port, reviewId, userId)
        );
    }

    // =============================== POST /reviews ======================================

    @Test
    public void shouldCreateReview() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();

        FilmReview createdFilmReview = testRestTemplate.postForObject(reviewsUrl, initialFilmReview, FilmReview.class);
        FilmReview savedFilmReview = filmReviewStorage.getReviewById(createdFilmReview.getReviewId()).get();

        assertNotNull(savedFilmReview, "Review not created");
        assertEquals(createdFilmReview.getReviewId(), savedFilmReview.getReviewId(), "Created review id is wrong");
        assertEquals(initialFilmReview.getFilmId(), savedFilmReview.getFilmId(), "Film id is wrong");
        assertEquals(initialFilmReview.getUserId(), savedFilmReview.getUserId(), "User id is wrong");
        assertEquals(initialFilmReview.getContent(), savedFilmReview.getContent(), "Content is wrong");
        assertEquals(initialFilmReview.getIsPositive(), savedFilmReview.getIsPositive(), "IsPositive is wrong");
    }

    @Test
    public void shouldReturn400IfIdSendingWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setReviewId(1L);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserDoesNotExistWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setUserId(999L);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setUserId(-1L);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfUserIdIsNullWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setUserId(null);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfFilmDoesNotExistWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setFilmId(999L);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfFilmIdIsNegativeWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setFilmId(-1L);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfFilmIdIsNullWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setFilmId(null);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfContentIsNullWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setContent(null);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfContentLengthOverLimitWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setContent("A".repeat(5001));
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfContentIsBlankWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setContent("  ");
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfIsPositiveIsNullWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setIsPositive(null);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldIgnoreUsefulWhenReviewIsCreated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setUseful(20);

        FilmReview createdFilmReview = testRestTemplate.postForObject(reviewsUrl, initialFilmReview, FilmReview.class);
        FilmReview savedFilmReview = filmReviewStorage.getReviewById(createdFilmReview.getReviewId()).get();

        assertEquals(0, savedFilmReview.getUseful(), "Should not use field");
    }

    // =============================== PUT /reviews ======================================

    @Test
    public void shouldUpdateReview() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        initialFilmReview.setContent("Updated review content");
        initialFilmReview.setIsPositive(false);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        testRestTemplate.exchange(reviewsUrl, HttpMethod.PUT, entity, FilmReview.class);
        FilmReview updatedFilmReview = filmReviewStorage.getReviewById(savedReviewId).get();

        assertEquals(false, updatedFilmReview.getIsPositive(), "Field is not updated");
        assertEquals("Updated review content", updatedFilmReview.getContent(), "Field is not updated");
    }

    @Test
    public void shouldReturn400IfReviewIdAbsentWhenReviewIsUpdated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfReviewIdIsNegativeWhenReviewIsUpdated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setReviewId(-1L);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfReviewIsNotExistWhenReviewIsUpdated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        initialFilmReview.setReviewId(999L);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldIgnoreUserIdWhenReviewIsUpdated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long initialUserId = initialFilmReview.getUserId();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        initialFilmReview.setUserId(null);

        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        testRestTemplate.exchange(reviewsUrl, HttpMethod.PUT, entity, FilmReview.class);
        FilmReview updatedFilmReview = filmReviewStorage.getReviewById(savedReviewId).get();
        assertEquals(initialUserId, updatedFilmReview.getUserId(), "User id has been changed");
    }

    @Test
    public void shouldIgnoreFilmIdWhenReviewIsUpdated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long initialFilmId = initialFilmReview.getFilmId();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        initialFilmReview.setUserId(null);

        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        testRestTemplate.exchange(reviewsUrl, HttpMethod.PUT, entity, FilmReview.class);
        FilmReview updatedFilmReview = filmReviewStorage.getReviewById(savedReviewId).get();
        assertEquals(initialFilmId, updatedFilmReview.getFilmId(), "Film id has been changed");
    }

    @Test
    public void shouldReturn400IfContentLengthOverLimitWhenReviewIsUpdated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        initialFilmReview.setReviewId(savedReviewId);
        initialFilmReview.setContent("A".repeat(5001));
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfContentIsAbsentWhenReviewIsUpdated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        initialFilmReview.setReviewId(savedReviewId);
        initialFilmReview.setContent(null);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfContentIsBlankWhenReviewIsUpdated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        initialFilmReview.setReviewId(savedReviewId);
        initialFilmReview.setContent("   ");
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfIsPositiveIsAbsentWhenReviewIsUpdated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        initialFilmReview.setReviewId(savedReviewId);
        initialFilmReview.setIsPositive(null);
        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldIgnoreUsefulWhenReviewIsUpdated() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        int initialUseful = initialFilmReview.getUseful();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        initialFilmReview.setUseful(999);

        HttpEntity<FilmReview> entity = new HttpEntity<>(initialFilmReview, applicationJsonHeaders);

        testRestTemplate.exchange(reviewsUrl, HttpMethod.PUT, entity, FilmReview.class);
        FilmReview updatedFilmReview = filmReviewStorage.getReviewById(savedReviewId).get();
        assertEquals(initialUseful, updatedFilmReview.getUseful(), "Useful has been changed");
    }

    // =============================== DELETE /reviews/{id} ======================================

    @Test
    public void shouldDeleteReviewById() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();

        testRestTemplate.exchange(
                getDeleteOrGetByURI(savedReviewId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertTrue(filmReviewStorage.getReviewById(savedReviewId).isEmpty(), "Review is not deleted");
    }

    @Test
    public void shouldReturn404IfReviewNotExistWhenReviewIsDeleted() {

        ResponseEntity<String> response = testRestTemplate.exchange(
                getDeleteOrGetByURI(999L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfReviewIdIsNegativeWhenReviewIsDeleted() {

        ResponseEntity<String> response = testRestTemplate.exchange(
                getDeleteOrGetByURI(-1L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    // =============================== GET /reviews/{id} ======================================

    @Test
    public void shouldGetReviewById() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        initialFilmReview.setReviewId(savedReviewId);

        FilmReview gottenFilmReview = testRestTemplate.exchange(
                getDeleteOrGetByURI(savedReviewId),
                HttpMethod.GET,
                null,
                FilmReview.class
        ).getBody();

        assertEquals(initialFilmReview, gottenFilmReview, "Requested review not equals initial review");
    }

    @Test
    public void shouldReturn404IfReviewNotExistWhenGetReviewById() {

        ResponseEntity<String> response = testRestTemplate.exchange(
                getDeleteOrGetByURI(999L),
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfReviewIdIsNegativeWhenGetReviewById() {

        ResponseEntity<String> response = testRestTemplate.exchange(
                getDeleteOrGetByURI(-1L),
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldCalculateUsefulIfOnlyLikesExist() {
        FilmReview filmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long filmReviewId = filmReviewStorage.addReview(filmReview).getReviewId();
        Long userId1 = testDataProducer.addDefaultUserToDB();
        filmReviewStorage.addLikeToReview(filmReviewId, userId1);

        FilmReview requestedFilmReview = testRestTemplate.exchange(
                getDeleteOrGetByURI(filmReviewId),
                HttpMethod.GET,
                null,
                FilmReview.class
        ).getBody();

        assertEquals(1, requestedFilmReview.getUseful(), "Useful is wrong");
    }

    @Test
    public void shouldCalculateUsefulIfOnlyDislikesExist() {
        FilmReview filmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long filmReviewId = filmReviewStorage.addReview(filmReview).getReviewId();
        Long userId1 = testDataProducer.addDefaultUserToDB();
        filmReviewStorage.addDislikeToReview(filmReviewId, userId1);

        FilmReview requestedFilmReview = testRestTemplate.exchange(
                getDeleteOrGetByURI(filmReviewId),
                HttpMethod.GET,
                null,
                FilmReview.class
        ).getBody();

        assertEquals(-1, requestedFilmReview.getUseful(), "Useful is wrong");
    }

    @Test
    public void shouldCalculateUsefulIfLikesAndDislikesExist() {
        FilmReview filmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long filmReviewId = filmReviewStorage.addReview(filmReview).getReviewId();
        Long userId1 = testDataProducer.addDefaultUserToDB();
        Long userId2 = testDataProducer.addDefaultUserToDB();
        Long userId3 = testDataProducer.addDefaultUserToDB();
        Long userId4 = testDataProducer.addDefaultUserToDB();
        Long userId5 = testDataProducer.addDefaultUserToDB();
        filmReviewStorage.addDislikeToReview(filmReviewId, userId1);
        filmReviewStorage.addDislikeToReview(filmReviewId, userId2);
        filmReviewStorage.addLikeToReview(filmReviewId, userId3);
        filmReviewStorage.addLikeToReview(filmReviewId, userId4);
        filmReviewStorage.addLikeToReview(filmReviewId, userId5);

        FilmReview requestedFilmReview = testRestTemplate.exchange(
                getDeleteOrGetByURI(filmReviewId),
                HttpMethod.GET,
                null,
                FilmReview.class
        ).getBody();

        assertEquals(1, requestedFilmReview.getUseful(), "Useful is wrong");
    }

    // =============================== GET /reviews?filmId={filmId}&count={count} ======================================

    @Test
    public void shouldGetAllReviewsForFilmWithCountLimit() {
        Long filmId = filmReviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(11);

        List<FilmReview> requestedFilmReviews = testRestTemplate.exchange(
                getGetReviewsByFilmIdWithCountURI(filmId, 5),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FilmReview>>() {
                }
        ).getBody();

        assertEquals(5, requestedFilmReviews.size(), "Reviews number is wrong");
        assertEquals(filmId, requestedFilmReviews.get(0).getFilmId(), "Review is not for requested film");
        assertEquals(11, requestedFilmReviews.get(0).getUseful(), "Review sort is wrong");
    }

    @Test
    public void shouldGetAllReviewsForFilmWithoutCountLimit() {
        Long filmId = filmReviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(11);

        List<FilmReview> requestedFilmReviews = testRestTemplate.exchange(
                getGetReviewsByFilmIdNoCountURI(filmId),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FilmReview>>() {
                }
        ).getBody();

        assertEquals(10, requestedFilmReviews.size(), "Reviews number is wrong");
        assertEquals(filmId, requestedFilmReviews.get(0).getFilmId(), "Review is not for requested film");
        assertEquals(11, requestedFilmReviews.get(0).getUseful(), "Review sort is wrong");
    }

    @Test
    public void shouldReturnEmptyListIfNoReviewsForFilm() {
        Long filmId = testDataProducer.addDefaultFilmToDB();

        List<FilmReview> requestedFilmReviews = testRestTemplate.exchange(
                getGetReviewsByFilmIdWithCountURI(filmId, 5),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FilmReview>>() {
                }
        ).getBody();

        assertTrue(requestedFilmReviews.isEmpty(), "Wrong review list size");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetAllReviewsWithCountLimit() {
        filmReviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(10);
        filmReviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(10);

        List<FilmReview> requestedFilmReviews = testRestTemplate.exchange(
                getGetReviewsNoFilmIdWithCountURI(5),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FilmReview>>() {
                }
        ).getBody();

        assertEquals(5, requestedFilmReviews.size(), "Reviews number is wrong");
        assertEquals(10, requestedFilmReviews.get(0).getUseful(), "Review sort is wrong");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetAllReviewsWithoutCountLimit() {
        filmReviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(10);
        filmReviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(10);

        List<FilmReview> requestedFilmReviews = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FilmReview>>() {
                }
        ).getBody();

        assertEquals(10, requestedFilmReviews.size(), "Reviews number is wrong");
        assertEquals(10, requestedFilmReviews.get(0).getUseful(), "Review sort is wrong");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturnEmptyListIfNoReviewsExist() {

        List<FilmReview> requestedFilmReviews = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FilmReview>>() {
                }
        ).getBody();

        assertTrue(requestedFilmReviews.isEmpty(), "Wrong review list size");
    }

    @Test
    public void shouldReturn404IfFilmNotExistsWhenGettingReviewForFilm() {

        ResponseEntity<String> response = testRestTemplate.exchange(
                getGetReviewsByFilmIdWithCountURI(999L, 5),
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfFilmIdIsNegativeWhenGettingReviewForFilm() {

        ResponseEntity<String> response = testRestTemplate.exchange(
                getGetReviewsByFilmIdWithCountURI(-1L, 5),
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfCountIsNegativeWhenGettingReviewForFilm() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        filmReviewStorage.addReview(initialFilmReview).getReviewId();
        Long filmId = initialFilmReview.getFilmId();

        ResponseEntity<String> response = testRestTemplate.exchange(
                getGetReviewsByFilmIdWithCountURI(filmId, -5),
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    // =============================== PUT /reviews/{id}/like/{userId} ======================================

    @Test
    public void shouldIncreaseUsefulAfterLike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();

        testRestTemplate.exchange(getReviewLikesURI(savedReviewId, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        FilmReview updatedFilmReview = filmReviewStorage.getReviewById(savedReviewId).get();
        assertEquals(1, updatedFilmReview.getUseful(), "Useful is wrong");
    }

    @Test
    public void shouldNotIncreaseUsefulAfterLikeFromSameUser() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();
        testRestTemplate.exchange(getReviewLikesURI(savedReviewId, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        testRestTemplate.exchange(getReviewLikesURI(savedReviewId, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        FilmReview updatedFilmReview = filmReviewStorage.getReviewById(savedReviewId).get();
        assertEquals(1, updatedFilmReview.getUseful(), "Useful should not been increased");
    }

    @Test
    public void shouldReturn404IfUserNotExistWhenAddingLike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewLikesURI(savedReviewId, 999L),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenAddingLike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewLikesURI(savedReviewId, -1L),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfReviewNotExistWhenAddingLike() {
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewLikesURI(999L, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfReviewIdIsNegativeWhenAddingLike() {
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewLikesURI(-1L, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    // =============================== DELETE /reviews/{id}/like/{userId} ======================================

    @Test
    public void shouldDecreaseUsefulAfterLikeDelete() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();
        filmReviewStorage.addLikeToReview(savedReviewId, userId);

        testRestTemplate.exchange(getReviewLikesURI(savedReviewId, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        FilmReview updatedFilmReview = filmReviewStorage.getReviewById(savedReviewId).get();
        assertEquals(0, updatedFilmReview.getUseful(), "Useful is wrong");
    }

    @Test
    public void shouldReturn404IfLikeNotExistWhenDeletingLike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewLikesURI(savedReviewId, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserNotExistWhenDeletingLike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewLikesURI(savedReviewId, 999L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenDeletingLike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewLikesURI(savedReviewId, -1L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfReviewNotExistWhenDeletingLike() {
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewLikesURI(999L, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfReviewIdIsNegativeWhenDeletingLike() {
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewLikesURI(-1L, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    // =============================== PUT /reviews/{id}/dislike/{userId} ======================================

    @Test
    public void shouldDecreaseUsefulAfterDislike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();
        filmReviewStorage.addLikeToReview(savedReviewId, userId);

        testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        FilmReview updatedFilmReview = filmReviewStorage.getReviewById(savedReviewId).get();
        assertEquals(0, updatedFilmReview.getUseful(), "Useful is wrong");
    }

    @Test
    public void shouldNotDecreaseUsefulAfterDislikeFromSameUser() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();
        testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        FilmReview updatedFilmReview = filmReviewStorage.getReviewById(savedReviewId).get();
        assertEquals(-1, updatedFilmReview.getUseful(), "Useful should not been increased");
    }

    @Test
    public void shouldReturn404IfUserNotExistWhenAddingDislike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, 999L),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenAddingDislike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, -1L),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfReviewNotExistWhenAddingDislike() {
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewDislikesURI(999L, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfReviewIdIsNegativeWhenAddingDislike() {
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewDislikesURI(-1L, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    // =============================== DELETE /reviews/{id}/dislike/{userId} ======================================

    @Test
    public void shouldDecreaseUsefulAfterDislikeDelete() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();
        filmReviewStorage.addDislikeToReview(savedReviewId, userId);

        testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        FilmReview updatedFilmReview = filmReviewStorage.getReviewById(savedReviewId).get();
        assertEquals(0, updatedFilmReview.getUseful(), "Useful is wrong");
    }

    @Test
    public void shouldReturn404IfDislikeNotExistWhenDeletingLike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserNotExistWhenDeletingDislike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, 999L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenDeletingDislike() {
        FilmReview initialFilmReview = filmReviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = filmReviewStorage.addReview(initialFilmReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, -1L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfReviewNotExistWhenDeletingDislike() {
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewDislikesURI(999L, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfReviewIdIsNegativeWhenDeletingDislike() {
        Long userId = testDataProducer.addDefaultUserToDB();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewDislikesURI(-1L, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

}
