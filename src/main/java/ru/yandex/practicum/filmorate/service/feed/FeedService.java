package ru.yandex.practicum.filmorate.service.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.model.feed.OperationType;
import ru.yandex.practicum.filmorate.repository.feed.FeedStorage;
import ru.yandex.practicum.filmorate.service.validator.UserFieldsValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedStorage feedStorage;
    private final UserFieldsValidator userFieldsValidator;

    public List<Feed> getFeedListById(Long userId) {
        userFieldsValidator.checkIfPresentById(userId);
        return feedStorage.getUserFeed(userId);
    }

    public void reviewTest() {
        long userId = 1;
        long reviewId = 1;
        feedStorage.addEvent(userId, reviewId, EventType.REVIEW, OperationType.ADD);
        feedStorage.addEvent(userId, reviewId, EventType.REVIEW, OperationType.REMOVE);
        feedStorage.addEvent(userId, reviewId, EventType.REVIEW, OperationType.UPDATE);
    }
}
