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

    public Map<String, Object> mapToDb() {
        Map<String, Object> userValues = new HashMap<>();
        userValues.put("user_name", name);
        userValues.put("login", login);
        userValues.put("email", email);
        userValues.put("birthday", birthday);

        return userValues;
    }
}
