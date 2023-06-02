package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotExistsException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.film.DirectorDao;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DirectorDaoImplTest {
    @Autowired
    private final DirectorDao directorDao;

    @Test
    @Order(1)
    void testAddAndFindById() {
        Director directorTest = Director.builder()
                .name("Director name 1")
                .build();

        directorDao.add(directorTest);
        Optional<Director> directorOptional = Optional.ofNullable(directorDao.findById(1));
        assertThat(directorOptional)
                .isPresent()
                .hasValueSatisfying(director ->
                        assertThat(director).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "Director name 1")
                );
    }

    @Test
    @Order(2)
    void testUpdate() {
        Director directorTestUpdate = Director.builder()
                .id(1)
                .name("Update Director name 1")
                .build();

        directorDao.update(directorTestUpdate);
        Optional<Director> directorOptional = Optional.ofNullable(directorDao.findById(1));
        assertThat(directorOptional)
                .isPresent()
                .hasValueSatisfying(director ->
                        assertThat(director).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "Update Director name 1")
                );
    }

    @Test
    @Order(3)
    void testFindAll() {
        Director directorTest2 = Director.builder()
                .id(20)
                .name("director test 2")
                .build();
        Director directorTest3 = Director.builder()
                .id(30)
                .name("director test 3")
                .build();
        directorDao.add(directorTest2);
        directorDao.add(directorTest3);
        Optional<List<Director>> optionalDirectorList = Optional.ofNullable(directorDao.findAll());
        assertThat(optionalDirectorList)
                .isPresent()
                .hasValueSatisfying(directors ->
                        assertThat(directors.get(0)).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "Update Director name 1")
                )
                .hasValueSatisfying(directors ->
                        assertThat(directors.get(1)).hasFieldOrPropertyWithValue("id", 2)
                                .hasFieldOrPropertyWithValue("name", "director test 2")
                )
                .hasValueSatisfying(directors ->
                        assertThat(directors.get(2)).hasFieldOrPropertyWithValue("id", 3)
                                .hasFieldOrPropertyWithValue("name", "director test 3")
                );

    }

    @Test
    @Order(4)
    void testRemove() {
        Director directorTest = Director.builder()
                .id(1)
                .name("Director name 1")
                .build();

        directorDao.remove(directorTest.getId());
        NotExistsException ex = Assertions.assertThrows(NotExistsException.class, () ->
                directorDao.findById(1));
        assertEquals("Director with id 1 does not exist", ex.getMessage());

    }

}