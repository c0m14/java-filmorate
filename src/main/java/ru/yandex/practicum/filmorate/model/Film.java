package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {

    @PositiveOrZero
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    @Size(max = 200)
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    @Positive
    private Integer duration;
    @Builder.Default
    private Long likesCount = 0L;
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
    @NotNull
    private RatingMPA mpa;

    public Film(
            String name,
            String description,
            LocalDate releaseDate,
            int duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

    public Film(Long id, String name, String description, LocalDate releaseDate, int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

    public Map<String, Object> mapToDb() {
        Map<String, Object> filmValues = new HashMap<>();

        //Определяем маппинг только для полей, чьи проверки не зависят от БД
        filmValues.put("film_name", name);
        filmValues.put("description", description);
        filmValues.put("release_date", releaseDate);
        filmValues.put("duration", duration);

        return filmValues;
    }
}
