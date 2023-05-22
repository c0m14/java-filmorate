package ru.yandex.practicum.filmorate.repository.review;

import ru.yandex.practicum.filmorate.model.Review;

public interface ReviewStorage {

    Review addReview(Review review);
}
