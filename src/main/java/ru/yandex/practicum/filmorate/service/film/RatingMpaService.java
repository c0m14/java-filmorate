package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.MpaRatingNotExistException;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.repository.film.h2.RatingMpaDao;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingMpaService {
    private final RatingMpaDao ratingMpaDao;

    public RatingMPA getMpaById(int mapId) {
        if (mapId <= 0) {
            throw new MpaRatingNotExistException(
                    String.format("Mpa Reting with id %d does not exist", mapId)
            );
        }
        return ratingMpaDao.getMpaByIdFromDb(mapId);
    }

    public List<RatingMPA> getAllMpa() {
        return ratingMpaDao.getAllMpa();
    }

}
