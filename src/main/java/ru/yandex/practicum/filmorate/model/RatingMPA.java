package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class RatingMPA {
    private int id;
    private RatingMPAName name;

   private enum RatingMPAName {
        G,
        PG,
        PG13,
        R,
        NC17
    }
}
