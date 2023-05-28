package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.film.DirectorDao;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDao directorDao;

    public Director findById(Integer directorId) {
        return directorDao.findById(directorId);
    }

    public List<Director> findAll() {
        return directorDao.findAll();
    }

    public Director add(Director director) {
        return directorDao.add(director);
    }

    public Director update(Director director) {
        return directorDao.update(director);
    }

    public void remove(Integer directorId) {
        directorDao.remove(directorId);
    }

}
