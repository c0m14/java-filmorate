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
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.review.ReviewStorage;
import ru.yandex.practicum.filmorate.util.ReviewTestDataProducer;
import ru.yandex.practicum.filmorate.util.TestDataProducer;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReviewTest {
    private static final String HOST = "http://localhost:";
    @Value(value = "${local.server.port}")
    private int port;
    HttpHeaders applicationJsonHeaders;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private ReviewTestDataProducer reviewTestDataProducer;
    @Autowired
    private TestDataProducer testDataProducer;
    @Autowired
    private ReviewStorage reviewStorage;

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();

        Review createdReview = testRestTemplate.postForObject(reviewsUrl, initialReview, Review.class);
        Review savedReview = reviewStorage.getReviewById(createdReview.getReviewId()).get();

        assertNotNull(savedReview, "Review not created");
        assertEquals(createdReview.getReviewId(), savedReview.getReviewId(), "Created review id is wrong");
        assertEquals(initialReview.getFilmId(), savedReview.getFilmId(), "Film id is wrong");
        assertEquals(initialReview.getUserId(), savedReview.getUserId(), "User id is wrong");
        assertEquals(initialReview.getContent(), savedReview.getContent(), "Content is wrong");
        assertEquals(initialReview.getIsPositive(), savedReview.getIsPositive(), "IsPositive is wrong");
    }

    @Test
    public void shouldReturn400IfIdSendingWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setReviewId(1L);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserDoesNotExistWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setUserId(999L);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setUserId(-1L);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfUserIdIsNullWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setUserId(null);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfFilmDoesNotExistWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setFilmId(999L);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfFilmIdIsNegativeWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setFilmId(-1L);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfFilmIdIsNullWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setFilmId(null);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfContentIsNullWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setContent(null);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfContentLengthOverLimitWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setContent("A".repeat(5001));
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfContentIsBlankWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setContent("  ");
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn400IfIsPositiveIsNullWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setIsPositive(null);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        ResponseEntity<String> response = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.POST,
                entity,
                String.class);

        assertEquals(HttpStatus.valueOf(400), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldIgnoreUsefulWhenReviewIsCreated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setUseful(20);

        Review createdReview = testRestTemplate.postForObject(reviewsUrl, initialReview, Review.class);
        Review savedReview = reviewStorage.getReviewById(createdReview.getReviewId()).get();

        assertEquals(0, savedReview.getUseful(), "Should not use field");
    }

    // =============================== PUT /reviews ======================================

    @Test
    public void shouldUpdateReview() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        initialReview.setContent("Updated review content");
        initialReview.setIsPositive(false);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        testRestTemplate.exchange(reviewsUrl, HttpMethod.PUT, entity, Review.class);
        Review updatedReview = reviewStorage.getReviewById(savedReviewId).get();

        assertEquals(false, updatedReview.getIsPositive(), "Field is not updated");
        assertEquals("Updated review content", updatedReview.getContent(), "Field is not updated");
    }

    @Test
    public void shouldReturn400IfReviewIdAbsentWhenReviewIsUpdated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setReviewId(-1L);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        initialReview.setReviewId(999L);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long initialUserId = initialReview.getUserId();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        initialReview.setUserId(null);

        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        testRestTemplate.exchange(reviewsUrl, HttpMethod.PUT, entity, Review.class);
        Review updatedReview = reviewStorage.getReviewById(savedReviewId).get();
        assertEquals(initialUserId, updatedReview.getUserId(), "User id has been changed");
    }

    @Test
    public void shouldIgnoreFilmIdWhenReviewIsUpdated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long initialFilmId = initialReview.getFilmId();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        initialReview.setUserId(null);

        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        testRestTemplate.exchange(reviewsUrl, HttpMethod.PUT, entity, Review.class);
        Review updatedReview = reviewStorage.getReviewById(savedReviewId).get();
        assertEquals(initialFilmId, updatedReview.getFilmId(), "Film id has been changed");
    }

    @Test
    public void shouldReturn400IfContentLengthOverLimitWhenReviewIsUpdated() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        initialReview.setReviewId(savedReviewId);
        initialReview.setContent("A".repeat(5001));
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        initialReview.setReviewId(savedReviewId);
        initialReview.setContent(null);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        initialReview.setReviewId(savedReviewId);
        initialReview.setContent("   ");
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        initialReview.setReviewId(savedReviewId);
        initialReview.setIsPositive(null);
        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        int initialUseful = initialReview.getUseful();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        initialReview.setUseful(999);

        HttpEntity<Review> entity = new HttpEntity<>(initialReview, applicationJsonHeaders);

        testRestTemplate.exchange(reviewsUrl, HttpMethod.PUT, entity, Review.class);
        Review updatedReview = reviewStorage.getReviewById(savedReviewId).get();
        assertEquals(initialUseful, updatedReview.getUseful(), "Useful has been changed");
    }

    // =============================== DELETE /reviews/{id} ======================================

    @Test
    public void shouldDeleteReviewById() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();

        testRestTemplate.exchange(
                getDeleteOrGetByURI(savedReviewId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertTrue(reviewStorage.getReviewById(savedReviewId).isEmpty(), "Review is not deleted");
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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        initialReview.setReviewId(savedReviewId);

        Review gottenReview = testRestTemplate.exchange(
                getDeleteOrGetByURI(savedReviewId),
                HttpMethod.GET,
                null,
                Review.class
        ).getBody();

        assertEquals(initialReview, gottenReview, "Requested review not equals initial review");
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

    // =============================== GET /reviews?filmId={filmId}&count={count} ======================================

    @Test
    public void shouldGetAllReviewsForFilmWithCountLimit() {
        Long filmId = reviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(11);

        List<Review> requestedReviews = testRestTemplate.exchange(
                getGetReviewsByFilmIdWithCountURI(filmId, 5),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Review>>() {
                }
        ).getBody();

        assertEquals(5, requestedReviews.size(), "Reviews number is wrong");
        assertEquals(filmId, requestedReviews.get(0).getFilmId(), "Review is not for requested film");
        assertEquals(11, requestedReviews.get(0).getUseful(), "Review sort is wrong");
    }

    @Test
    public void shouldGetAllReviewsForFilmWithoutCountLimit() {
        Long filmId = reviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(11);

        List<Review> requestedReviews = testRestTemplate.exchange(
                getGetReviewsByFilmIdNoCountURI(filmId),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Review>>() {
                }
        ).getBody();

        assertEquals(10, requestedReviews.size(), "Reviews number is wrong");
        assertEquals(filmId, requestedReviews.get(0).getFilmId(), "Review is not for requested film");
        assertEquals(11, requestedReviews.get(0).getUseful(), "Review sort is wrong");
    }

    @Test
    public void shouldReturnEmptyListIfNoReviewsForFilm() {
        Long filmId = testDataProducer.addDefaultFilmToDB();

        List<Review> requestedReviews = testRestTemplate.exchange(
                getGetReviewsByFilmIdWithCountURI(filmId, 5),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Review>>() {
                }
        ).getBody();

        assertTrue(requestedReviews.isEmpty(), "Wrong review list size");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetAllReviewsWithCountLimit() {
        reviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(10);
        reviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(10);

        List<Review> requestedReviews = testRestTemplate.exchange(
                getGetReviewsNoFilmIdWithCountURI(5),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Review>>() {
                }
        ).getBody();

        assertEquals(5, requestedReviews.size(), "Reviews number is wrong");
        assertEquals(10, requestedReviews.get(0).getUseful(), "Review sort is wrong");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetAllReviewsWithoutCountLimit() {
        reviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(10);
        reviewTestDataProducer.createReviewsWithUsefulToFilmAndReturnFilmId(10);

        List<Review> requestedReviews = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Review>>() {
                }
        ).getBody();

        assertEquals(10, requestedReviews.size(), "Reviews number is wrong");
        assertEquals(10, requestedReviews.get(0).getUseful(), "Review sort is wrong");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldReturnEmptyListIfNoReviewsExist() {

        List<Review> requestedReviews = testRestTemplate.exchange(
                reviewsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Review>>() {
                }
        ).getBody();

        assertTrue(requestedReviews.isEmpty(), "Wrong review list size");
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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        reviewStorage.addReview(initialReview).getReviewId();
        Long filmId = initialReview.getFilmId();

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();

        testRestTemplate.exchange(getReviewLikesURI(savedReviewId, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        Review updatedReview = reviewStorage.getReviewById(savedReviewId).get();
        assertEquals(1, updatedReview.getUseful(), "Useful is wrong");
    }

    @Test
    public void shouldNotIncreaseUsefulAfterLikeFromSameUser() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
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

        Review updatedReview = reviewStorage.getReviewById(savedReviewId).get();
        assertEquals(1, updatedReview.getUseful(), "Useful should not been increased");
    }

    @Test
    public void shouldReturn404IfUserNotExistWhenAddingLike() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewLikesURI(savedReviewId, 999L),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenAddingLike() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();
        reviewStorage.addLikeToReview(savedReviewId, userId);

        testRestTemplate.exchange(getReviewLikesURI(savedReviewId, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        Review updatedReview = reviewStorage.getReviewById(savedReviewId).get();
        assertEquals(0, updatedReview.getUseful(), "Useful is wrong");
    }

    @Test
    public void shouldReturn404IfLikeNotExistWhenDeletingLike() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewLikesURI(savedReviewId, 999L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenDeletingLike() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();
        reviewStorage.addLikeToReview(savedReviewId, userId);

        testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, userId),
                HttpMethod.PUT,
                null,
                String.class
        );

        Review updatedReview = reviewStorage.getReviewById(savedReviewId).get();
        assertEquals(0, updatedReview.getUseful(), "Useful is wrong");
    }

    @Test
    public void shouldNotDecreaseUsefulAfterDislikeFromSameUser() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
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

        Review updatedReview = reviewStorage.getReviewById(savedReviewId).get();
        assertEquals(-1, updatedReview.getUseful(), "Useful should not been increased");
    }

    @Test
    public void shouldReturn404IfUserNotExistWhenAddingDislike() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, 999L),
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenAddingDislike() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();

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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
        Long userId = testDataProducer.addDefaultUserToDB();
        reviewStorage.addDislikeToReview(savedReviewId, userId);

        testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, userId),
                HttpMethod.DELETE,
                null,
                String.class
        );

        Review updatedReview = reviewStorage.getReviewById(savedReviewId).get();
        assertEquals(0, updatedReview.getUseful(), "Useful is wrong");
    }

    @Test
    public void shouldReturn404IfDislikeNotExistWhenDeletingLike() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();
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
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();

        ResponseEntity<String> response = testRestTemplate.exchange(getReviewDislikesURI(savedReviewId, 999L),
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertEquals(HttpStatus.valueOf(404), response.getStatusCode(), "Wrong status code");
    }

    @Test
    public void shouldReturn404IfUserIdIsNegativeWhenDeletingDislike() {
        Review initialReview = reviewTestDataProducer.getValidPositiveReview();
        Long savedReviewId = reviewStorage.addReview(initialReview).getReviewId();

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
