package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {

    @PositiveOrZero
    private int id;
    @NotNull
    @NotBlank
    @Email
    private String email;
    @NotNull
    @NotBlank
    private String login;
    private String name;
    @NotNull
    @Past
    private LocalDate birthday;

    public User(
            @JsonProperty("login")
            String login,
            @JsonProperty("name")
            String name,
            @JsonProperty("email")
            String email,
            @JsonProperty("birthday")
            LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

    public User(
            @JsonProperty("id")
            int id,
            @JsonProperty("login")
            String login,
            @JsonProperty("name")
            String name,
            @JsonProperty("email")
            String email,
            @JsonProperty("birthday")
            LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }
}
