package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @PositiveOrZero
    private Long id;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String login;
    private String name;
    @NotNull
    @Past
    private LocalDate birthday;
    private Map<Long, FriendConfirmationStatus> friends = new HashMap<>();

    public User(
            String login,
            String name,
            String email,
            LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }
}
