package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class FilmReview {
    @PositiveOrZero
    private Long reviewId;
    @NotBlank
    @Size(max = 5000)
    private String content;
    @NotNull
    private Boolean isPositive;
    @NotNull
    private Long userId;
    @NotNull
    private Long filmId;
    private int useful;

    public Map<String, Object> mapToDb() {
        Map<String, Object> userValues = new HashMap<>();
        userValues.put("review_id", reviewId);
        userValues.put("user_id", userId);
        userValues.put("film_id", filmId);
        userValues.put("content", content);
        userValues.put("is_positive", isPositive);

        return userValues;
    }
}
