package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.model.feed.OperationType;
import ru.yandex.practicum.filmorate.repository.feed.FeedStorage;
import ru.yandex.practicum.filmorate.repository.film.h2.FilmRepository;
import ru.yandex.practicum.filmorate.repository.filmReview.FilmReviewStorage;
import ru.yandex.practicum.filmorate.repository.user.UserStorage;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeedStorageTest {
    private final FeedStorage feedStorage;
    private final FilmReviewStorage filmReviewStorage;
    @Qualifier("H2UserRepository")
    private final UserStorage userStorage;

    @Qualifier("H2FilmRepository")
    private final FilmRepository filmRepository;
    private final FilmService filmService;

    @Test
    @Order(1)
    public void addFriendFeedTest() {
        User userTest = User.builder()
                .login("dolore")
                .name("Nick Name")
                .email("mail@mail.ru")
                .birthday(LocalDate.of(1946, 8, 20))
                .build();
        userStorage.addUser(userTest);
        User anotherUserTest = User.builder()
                .login("another friend")
                .name("another friendName")
                .email("anotherfriend@mail.ru")
                .birthday(LocalDate.of(1996, 8, 20))
                .build();
        userStorage.addUser(anotherUserTest);
        userStorage.addFriendToUser(1L, 2L);
        Feed feed = Feed.builder()
                .timestamp(feedStorage.getUserFeed(1L).get(0).getTimestamp())
                .userId(1L)
                .eventType(EventType.FRIEND)
                .operation(OperationType.ADD)
                .entityId(2L)
                .eventId(1L)
                .build();
        assertEquals(feedStorage.getUserFeed(1L).get(0), feed);
    }


    @Test
    @Order(2)
    public void removeFriendFeedTest() {
        userStorage.removeFriendFromUser(1L, 2L);
        Feed feed = Feed.builder()
                .timestamp(feedStorage.getUserFeed(1L).get(1).getTimestamp())
                .userId(1L)
                .eventType(EventType.FRIEND)
                .operation(OperationType.REMOVE)
                .entityId(2L)
                .eventId(2L)
                .build();
        assertEquals(feedStorage.getUserFeed(1L).get(1), feed);
    }

    @Test
    @Order(3)
    public void addLikeFeedTest() {
        Film film = Film.builder()
                .name("Titanic")
                .description("Drama")
                .releaseDate(LocalDate.of(1994, 1, 1))
                .duration(120)
                .mpa(new RatingMPA(1, "G"))
                .genres(Set.of(new Genre(2, "Драма"), new Genre(6, "Боевик")))
                .build();
        filmRepository.addFilm(film);
        filmService.giveLikeFromUserToFilm(1L, 1L);
        Feed feed = Feed.builder()
                .timestamp(feedStorage.getUserFeed(1L).get(2).getTimestamp())
                .userId(1L)
                .eventType(EventType.LIKE)
                .operation(OperationType.ADD)
                .entityId(1L)
                .eventId(3L)
                .build();
        assertEquals(feedStorage.getUserFeed(1L).get(2), feed);
    }

    @Test
    @Order(4)
    public void removeLikeFeedTest() {
        filmService.removeUserLikeFromFilm(1L, 1L);
        Feed feed = Feed.builder()
                .timestamp(feedStorage.getUserFeed(1L).get(3).getTimestamp())
                .userId(1L)
                .eventType(EventType.LIKE)
                .operation(OperationType.REMOVE)
                .entityId(1L)
                .eventId(4L)
                .build();
        assertEquals(feedStorage.getUserFeed(1L).get(3), feed);
    }

    @Test
    @Order(5)
    public void addReviewFeedTest() {
        FilmReview review = FilmReview.builder()
                .filmId(1L)
                .userId(1L)
                .content("Good")
                .isPositive(true)
                .build();
        filmReviewStorage.addReview(review);
        Feed feed = Feed.builder()
                .timestamp(feedStorage.getUserFeed(1L).get(4).getTimestamp())
                .userId(1L)
                .eventType(EventType.REVIEW)
                .operation(OperationType.ADD)
                .entityId(1L)
                .eventId(5L)
                .build();
        assertEquals(feedStorage.getUserFeed(1L).get(4), feed);
    }

    @Test
    @Order(6)
    public void updateReviewFeedTest() {
        FilmReview updateReview = FilmReview.builder()
                .filmId(1L)
                .userId(1L)
                .content("maybe Good")
                .isPositive(true)
                .reviewId(1L)
                .build();
        filmReviewStorage.updateReview(updateReview);
        Feed feed = Feed.builder()
                .timestamp(feedStorage.getUserFeed(1L).get(5).getTimestamp())
                .userId(1L)
                .eventType(EventType.REVIEW)
                .operation(OperationType.UPDATE)
                .entityId(1L)
                .eventId(6L)
                .build();
        assertEquals(feedStorage.getUserFeed(1L).get(5), feed);
    }

    @Test
    @Order(7)
    public void removeReviewFeedTest() {
        filmReviewStorage.deleteReview(1L);
        Feed feed = Feed.builder()
                .timestamp(feedStorage.getUserFeed(1L).get(6).getTimestamp())
                .userId(1L)
                .eventType(EventType.REVIEW)
                .operation(OperationType.REMOVE)
                .entityId(1L)
                .eventId(7L)
                .build();
        assertEquals(feedStorage.getUserFeed(1L).get(6), feed);
    }
}
