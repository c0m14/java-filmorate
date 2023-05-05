package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.repository.film.h2.RatingMpaDao;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingMpaService {
    private final RatingMpaDao ratingMpaDao;

    public RatingMPA getMpaById(int mapId) {
        return ratingMpaDao.getMpaByIdFromDb(mapId);
    }

    public List<RatingMPA> getAllMpa() {
        return ratingMpaDao.getAllMpa();
    }

}
