package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {

    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    public User(
            @JsonProperty("login")
            String login,
            @JsonProperty("name")
            String name,
            @JsonProperty("email")
            String email,
            @JsonProperty("birthday")
            LocalDate birthday)
    {
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
            LocalDate birthday)
    {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }
}
