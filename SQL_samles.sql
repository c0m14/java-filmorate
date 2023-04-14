-- ==== USERS ====

-- ==== GET all users ====
SELECT * 
FROM users;

-- ==== GET user by {id} ====
SELECT *
FROM users
WHERE id = {id};

-- ==== GET user friends by {user_id} ====

SELECT *
FROM users u
LEFT JOIN user_friend uf ON u.id = uf.user_id
WHERE u.id IN (SELECT friend_id
			   FROM user_friend
			   WHERE user_id = {user_id});

-- ==== GET common friends for {user1_id} with {user2_id} ====

SELECT *
FROM users
WHERE id IN (SELECT friend_id
			 FROM user_friend uf1
			 INNER JOIN user_friend uf2 ON uf1.id = uf2.id
			 WHERE (uf1.user_id = {user1_id}
			 AND uf2.user_id = {user2_id})
			 AND confirmation_status_id = 2 -- подтвержденный друг
			);



-- ==== FILMS ====

-- ==== GET all films ====
SELECT * 
FROM films;

-- ==== GET film by {id} ====
SELECT *
FROM films
WHERE id = {id};

-- ==== GET {count} popular films ====
SELECT *
FROM films
WHERE id IN (SELECT film_id
			 FROM user_film_likes
			 GROUP BY film_id
			 ORDER BY COUNT(user_id) DESC
			 LIMIT {count}
			 );