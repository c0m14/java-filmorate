package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {

    private int id;
    private final String name;
    private String description;
    private final LocalDate releaseDate;
    private final int duration;


    public Film(
            @JsonProperty("name")
            String name,
            @JsonProperty("description")
            String description,
            @JsonProperty("releaseDate")
            LocalDate releaseDate,
            @JsonProperty("duration")
            int duration)
    {
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
            int duration)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }
}
