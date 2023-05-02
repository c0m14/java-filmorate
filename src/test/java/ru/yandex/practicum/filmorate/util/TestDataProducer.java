package ru.yandex.practicum.filmorate.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.film.FilmStorage;
import ru.yandex.practicum.filmorate.repository.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TestDataProducer {
    @Qualifier("H2FilmRepository")
    private final FilmStorage filmStorage;
    @Qualifier("H2UserRepository")
    private final UserStorage userStorage;
    private final Set<Genre> correctGenres = Set.of(new Genre(2, "Драма"), new Genre(6, "Боевик"));
    private final Set<Genre> wrongGenres = Set.of(new Genre(999, "Драма"), new Genre(1000, "Боевик"));
    private final RatingMPA correctRatingMpa = new RatingMPA(1, "G");
    private final RatingMPA wrongRatingMpa = new RatingMPA(999, "G");

    private final Film filmWithoutGenres = Film.builder()
            .name("Titanic")
            .description("Drama")
            .releaseDate(LocalDate.of(1994, 1, 1))
            .duration(120)
            .mpa(correctRatingMpa)
            .build();

    private final Film filmWithGenres = Film.builder()
            .name("Titanic")
            .description("Drama")
            .releaseDate(LocalDate.of(1994, 1, 1))
            .duration(120)
            .mpa(correctRatingMpa)
            .genres(correctGenres)
            .build();

    private final Film filmWithWrongGenres = Film.builder()
            .name("Titanic")
            .description("Drama")
            .releaseDate(LocalDate.of(1994, 1, 1))
            .duration(120)
            .mpa(correctRatingMpa)
            .genres(wrongGenres)
            .build();

    private final Film filmWithWrongMpaId = Film.builder()
            .name("Titanic")
            .description("Drama")
            .releaseDate(LocalDate.of(1994, 1, 1))
            .duration(120)
            .mpa(wrongRatingMpa)
            .build();

    private final User defaultUser = User.builder()
            .name("name")
            .login("login")
            .email("email@domen.ru")
            .birthday(LocalDate.of(2000, 1, 1))
            .build();

    public Film getFilmWithoutGenres() {
        return filmWithoutGenres;
    }

    public Film getFilmWithGenres() {
        return filmWithGenres;
    }

    public Film getFilmWithWrongGenres() {
        return filmWithWrongGenres;
    }

    public Film getFilmWithWrongMpaId() {
        return filmWithWrongMpaId;
    }

    public Film getMutableFilm() {
        return Film.builder()
                .name("Titanic")
                .description("Drama")
                .releaseDate(LocalDate.of(1994, 1, 1))
                .duration(120)
                .mpa(correctRatingMpa)
                .build();
    }

    public Set<Genre> getCorrectGenres() {
        return correctGenres;
    }

    public Set<Genre> getWrongGenres() {
        return wrongGenres;
    }

    public User getDefaultMutableUser() {
        return User.builder()
                .name("name")
                .login("login")
                .email("email@domen.ru")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    public Long addDefaultUserToDB() {
        return userStorage.addUser(getDefaultMutableUser()).getId();
    }

    public Long addDefaultFilmToDB() {
        return filmStorage.addFilm(getMutableFilm()).getId();
    }

    public void createContextWithPopularFilms() {
        List<Long> filmsIds = new ArrayList<>();
        List<Long> usersIds = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            filmsIds.add(addDefaultFilmToDB());
            usersIds.add(addDefaultUserToDB());
        }
        for (Long filmsId : filmsIds) {
            for (Long usersId : usersIds) {
                filmStorage.giveLikeFromUserToFilm(filmsId, usersId);
            }
            usersIds.remove(usersIds.size() - 1);
        }
    }
}
