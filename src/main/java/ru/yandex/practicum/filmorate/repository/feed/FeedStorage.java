package ru.yandex.practicum.filmorate.repository.feed;

import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.model.feed.OperationType;

import java.util.List;

public interface FeedStorage {
    void addEvent(Feed feed);

    List<Feed> getUserFeed(Long userId);
}
