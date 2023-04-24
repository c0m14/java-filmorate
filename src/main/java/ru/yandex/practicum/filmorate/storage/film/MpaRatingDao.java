package ru.yandex.practicum.filmorate.storage.film;
import ru.yandex.practicum.filmorate.model.RatingMPA;


public interface MpaRatingDao {

     RatingMPA getMpaByIdFromDb(int mpaId);
}
