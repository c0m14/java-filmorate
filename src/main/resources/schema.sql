DROP TABLE IF EXISTS USERS CASCADE;
CREATE TABLE USERS (
                         USER_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                         USER_NAME VARCHAR NOT NULL,
                         LOGIN VARCHAR NOT NULL,
                         EMAIL VARCHAR NOT NULL,
                         BIRTHDAY DATE NOT NULL
);

DROP TABLE IF EXISTS USER_FRIEND CASCADE;
CREATE TABLE USER_FRIEND (
                               USER_ID INTEGER,
                               FRIEND_ID INTEGER,
                               CONFIRMATION_STATUS VARCHAR NOT NULL,
                               PRIMARY KEY (USER_ID, FRIEND_ID),
                               CONSTRAINT fk__user_id__users_user_id FOREIGN KEY (USER_ID) REFERENCES USERS (USER_ID),
                               CONSTRAINT fk__friend_id__users_user_id FOREIGN KEY (FRIEND_ID) REFERENCES USERS (USER_ID)
);

DROP TABLE IF EXISTS GENRE CASCADE;
CREATE TABLE GENRE (
                         GENRE_ID INTEGER PRIMARY KEY,
                         GENRE_NAME VARCHAR
);

DROP TABLE IF EXISTS MPA_RATING CASCADE;
CREATE TABLE MPA_RATING (
                              MPA_RATING_ID INTEGER PRIMARY KEY,
                              MPA_RATING_NAME VARCHAR
);

DROP TABLE IF EXISTS FILM CASCADE;
CREATE TABLE FILM (
                        FILM_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        FILM_NAME VARCHAR NOT NULL,
                        DESCRIPTION VARCHAR(200) NOT NULL,
                        RELEASE_DATE DATE NOT NULL,
                        DURATION INTEGER,
                        MPA_RATING_ID INTEGER,
                        CONSTRAINT fk__film__mpa_rating FOREIGN KEY ("MPA_RATING_ID")
                            REFERENCES MPA_RATING (MPA_RATING_ID)
);

DROP TABLE IF EXISTS FILM_GENRE CASCADE;
CREATE TABLE FILM_GENRE (
                              FILM_ID INTEGER,
                              GENRE_ID INTEGER,
                              PRIMARY KEY (FILM_ID, GENRE_ID),
                              CONSTRAINT fk__film_genre__film FOREIGN KEY (FILM_ID) REFERENCES FILM (FILM_ID),
                              CONSTRAINT fk__film_genre__genre FOREIGN KEY (GENRE_ID) REFERENCES GENRE (GENRE_ID)
);

DROP TABLE IF EXISTS USER_FILM_LIKES CASCADE;
CREATE TABLE USER_FILM_LIKES (
                                   USER_ID INTEGER,
                                   FILM_ID INTEGER,
                                   PRIMARY KEY (USER_ID, FILM_ID),
                                   CONSTRAINT fk__user_film_likes__users FOREIGN KEY (USER_ID)
                                       REFERENCES USERS (USER_ID),
                                   CONSTRAINT fk__user_film_likes__film FOREIGN KEY (FILM_ID)
                                        REFERENCES FILM (FILM_ID)
);

DROP TABLE IF EXISTS REVIEW CASCADE;
CREATE TABLE REVIEW (
                               REVIEW_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                               USER_ID INTEGER,
                               FILM_ID INTEGER,
                               CONTENT VARCHAR(5000) NOT NULL,
                               IS_POSITIVE BOOLEAN NOT NULL,
                               CONSTRAINT fk__review__users FOREIGN KEY (USER_ID)
                                    REFERENCES USERS (USER_ID),
                               CONSTRAINT fk__review__film FOREIGN KEY (FILM_ID)
                                    REFERENCES FILM (FILM_ID)
);

DROP TABLE IF EXISTS USER_REVIEW_LIKES CASCADE;
CREATE TABLE USER_REVIEW_LIKES (
                                   USER_ID INTEGER,
                                   REVIEW_ID INTEGER,
                                   PRIMARY KEY (USER_ID, REVIEW_ID),
                                   CONSTRAINT fk__user_review_likes__users FOREIGN KEY (USER_ID)
                                       REFERENCES USERS (USER_ID),
                                   CONSTRAINT fk__user_review_likes__review FOREIGN KEY (REVIEW_ID)
                                        REFERENCES REVIEW (REVIEW_ID)
);

DROP TABLE IF EXISTS USER_REVIEW_DISLIKES CASCADE;
CREATE TABLE USER_REVIEW_DISLIKES (
                                   USER_ID INTEGER,
                                   REVIEW_ID INTEGER,
                                   PRIMARY KEY (USER_ID, REVIEW_ID),
                                   CONSTRAINT fk__user_review_dislikes__users FOREIGN KEY (USER_ID)
                                       REFERENCES USERS (USER_ID),
                                   CONSTRAINT fk__user_review_dislikes__review FOREIGN KEY (REVIEW_ID)
                                        REFERENCES REVIEW (REVIEW_ID)
);
