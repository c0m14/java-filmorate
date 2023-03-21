package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@AllArgsConstructor
@Data
public class Film {

    @PositiveOrZero
    private int id;
    @NotNull
    @NotBlank
    private final String name;
    @NotBlank
    @Size(max = 200)
    private String description;
    @NotNull
    private final LocalDate releaseDate;
    @Positive
    private final int duration;

    public Film(
            @JsonProperty("name")
            String name,
            @JsonProperty("description")
            String description,
            @JsonProperty("releaseDate")
            LocalDate releaseDate,
            @JsonProperty("duration")
            int duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }
}
