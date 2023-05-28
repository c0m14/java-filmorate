package ru.yandex.practicum.filmorate.model;

import java.util.Set;

public class Constants {
    public static final String SORT_BY_LIKES = "likes";
    public static final String SORT_BY_YEAR = "year";

    public static final Set<String> SORTS = Set.of(SORT_BY_LIKES, SORT_BY_YEAR);
}
