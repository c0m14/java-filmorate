package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RatingMPA {
    private int id;
    private RatingMPAName name;

   public enum RatingMPAName {
        G,
        PG,
        PG13,
        R,
        NC17
    }
}
