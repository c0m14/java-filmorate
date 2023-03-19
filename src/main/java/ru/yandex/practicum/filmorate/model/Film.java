package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class Film {

    @NotNull
    @NotBlank
    private final String name;
    @NotNull
    private final LocalDate releaseDate;
    @Positive
    private final int duration;
    @PositiveOrZero
    private int id;
    @NotBlank
    @Size(max = 200)
    private String description;


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

    public Film(
            @JsonProperty("id")
            int id,
            @JsonProperty("name")
            String name,
            @JsonProperty("description")
            String description,
            @JsonProperty("releaseDate")
            LocalDate releaseDate,
            @JsonProperty("duration")
            int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }
}
