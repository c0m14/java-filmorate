package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class User {

    private final int id;
    private String email;
    private final String login;
    private String name;
    private LocalDate birthDay;

}
