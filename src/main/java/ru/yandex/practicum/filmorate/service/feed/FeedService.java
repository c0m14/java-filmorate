package ru.yandex.practicum.filmorate.service.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.feed.*;
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
}
