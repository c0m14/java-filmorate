package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class Review {
    @PositiveOrZero
    private Long reviewId;
    @NotBlank
    private String content;
    @NotNull
    private Boolean isPositive;
    @Positive
    private Long userId;
    @Positive
    private Long filmId;
    private int useful;

    public Map<String, Object> mapToDb() {
        Map<String, Object> userValues = new HashMap<>();
        userValues.put("review_id", reviewId);
        userValues.put("content", content);
        userValues.put("is_positive", isPositive);

        return userValues;
    }
}
