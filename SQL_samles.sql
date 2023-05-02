-- ==== USERS ====

-- ==== CREATE user ====
INSERT INTO users (user_name, login, email, birthday)
VALUES ({:userName}, {:userLogin}, {:userEmail}, {:userBirthday});

-- ==== UPDATE user ====
UPDATE users
SET user_name = {:userName}, login = {:login}, email = {:email}, birthday = {:birthday}
WHERE user_id = {:userId}

-- ==== ADD friend with {:friendId} to user with {:userId} ====
INSERT INTO user_friend (user_id, friend_id, confirmation_status)
VALUES ({:userId}, {:friendId}, {:confirmed})
ON CONFLICT DO UPDATE;

INSERT INTO user_friend (user_id, friend_id, confirmation_status)
VALUES ({:friendId}, {:userId}, {:pending})
ON CONFLICT DO UPDATE;

-- ==== REMOVE friend with {:friendId} from user with {:userId} ====
--Если друг не добавлял пользователя в друзья (статус WAITING_FOR_APPROVAL)
DELETE FROM user_friend
WHERE (user_id = {:userId} AND friend_id = {:friendId})
OR
(user_id = {:friendId} AND friend_id = {:userId});

--Если друг добавил пользователя в друзья (статус CONFIRMED)
UPDATE user_friend
SET confirmation_status = {:pending}
WHERE user_id = {:userId} AND friend_id= {:friendId}

-- ==== GET all users ====
SELECT user_id, user_name, login, email, birthday
FROM users;

-- ==== GET user by {:userId} ====
SELECT user_id, user_name, login, email, birthday
FROM users
WHERE user_id = {:userId};

-- ==== GET user friends by {:userId} ====

SELECT user_id, user_name, login, email, birthday
FROM users
WHERE user_id IN
	(SELECT friend_id
    FROM user_friend
    WHERE user_id = {:userId} AND confirmation_status = {:confirmed});

-- ==== GET common friends for {:userId} with {:otherUserId} ====

SELECT user_id, user_name, login, email, birthday
FROM users
WHERE user_id IN
    (SELECT friend_id FROM
        (SELECT user_id, friend_id
        FROM user_friend
        WHERE user_id = {:userId} AND confirmation_status = {:confirmed}
        UNION ALL
        SELECT user_id, friend_id
        FROM user_friend
        WHERE user_id = {:otherUserId} AND confirmation_status = {:confirmed})
    GROUP BY FRIEND_ID
    HAVING COUNT(USER_ID) > 1);

    

-- ==== FILMS ====

-- ==== ADD film ====
--Добавить филь с базовыми полями:
INSERT INTO film (film_name, description, release_date, duration)
VALUES ({:filmName}, {:description}, {:release_date}, {:duration});
--Если есть mpa_rating:
INSERT INTO film (film_id, mpa_rating_id)
KEY (film_id)
VALUES ({:filmId}, {:ratingMpaId})
ON CONFLICT DO UPDATE;
--Если есть список жанров для каждого жанра из списка:
INSERT INTO film_genre
VALUES ({:filmId}, {:genreId})
ON CONFLICT DO UPDATE;

-- ==== UPDATE film ====
--Обновить базовае поля:
UPDATE film
SET film_name = {:filmName}, description = {:description}, release_date = {:releaseDate}, duration = {:duration}
WHERE film_id = {:filmId}
--Если есть mpa_rating:
INSERT INTO film (film_id, mpa_rating_id)
KEY (film_id)
VALUES ({:filmId}, {:ratingMpaId})
ON CONFLICT DO UPDATE;
--Если есть список жанров для каждого жанра из списка:
DELETE
FROM film_genre
WHERE film_id = :filmId

INSERT INTO film_genre
VALUES ({:filmId}, {:genreId})
ON CONFLICT DO UPDATE;

-- ==== GET all films ====
SELECT film_id, film_name, description, release_date, duration, mpa_rating_id
FROM film 

-- ==== GET film by {id} ====
SELECT film_id, film_name, description, release_date, duration, mpa_rating_id
FROM film
WHERE film_id = {:filmId}

-- + добавить поля:
--rating mpa
SELECT mpa_rating_id, mpa_rating_name FROM mpa_rating
WHERE mpa_rating_id = {:mpaRatingId}

--список жанров
SELECT genre_id, genre_name
FROM genre
WHERE genre_id IN
    (SELECT genre_id
    FROM film_genre
    WHERE film_id = {:filmId})

--количество лайков
SELECT COUNT(user_id)
FROM user_film_likes
WHERE film_id = {:filmId}
GROUP BY film_id

-- ==== GET {count} popular films ====
SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, f.mpa_rating_id
FROM film AS f
LEFT JOIN user_film_likes AS likes ON f.film_id = likes.film_id
GROUP BY f.film_id
ORDER BY SUM(likes.user_id) DESC
LIMIT {:count}

-- + для каждого фильма добавить поля:
--rating mpa
SELECT mpa_rating_id, mpa_rating_name FROM mpa_rating
WHERE mpa_rating_id = {:mpaRatingId}

--список жанров
SELECT genre_id, genre_name
FROM genre
WHERE genre_id IN
    (SELECT genre_id
    FROM film_genre
    WHERE film_id = {:filmId})

--количество лайков
SELECT COUNT(user_id)
FROM user_film_likes
WHERE film_id = {:filmId}
GROUP BY film_id

