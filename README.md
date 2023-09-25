# java-filmorate
## summary
Это мой второй большой проект в рамках обучения Java на платформе Яндекс Практикум. 

Основная цель этого приложения - помочь пользователю найти фильм 
для просмотра ориентируясь на лайки и найти друзей по интересам

### my other edu projects
- [(1) Kanban (Трекер задач а-ля Jira)](https://github.com/c0m14/java-kanban/blob/main/README.md)
- [(3) ShareIt ("Шэринг-сервис" - вещи в аренду)](https://github.com/c0m14/java-shareit/blob/main/README.md)
- [(4) Explore-with-me (Агрегатор мероприятий а-ля Афиша, дипломный проект)](https://github.com/c0m14/graduate_java-explore-with-me/blob/main/README.md)

### stack

- Java 11
- Spring Boot
- Lombok
- JDBCTemplate
- H2 (as database)

## main educational purposes
- Освоить framework Spring Boot
  - Система сборки Maven и управление внешними зависимостями с помощью pom.xml
  - REST API
    - Обработка RequestBody / PathVariable / RequestParam
    - Валидация данных и обработка исключений
  - Логирование (Slf4j)
  - Lombok
  - Dependencies Injection в Spring
- Работа с БД
  - Освоить основы SQL
  - Спроектировать схему БД проекта
  - Использование JDBCTemplate


## database scheme
![](filmorate-database-scheme.png)
### comments
- Основные таблицы:
  - user: пользователи сервиса
  - film: список фильмов
- Справочники:
  - user
    - confirmation_status: статус заявок в друзья
  - film
    - genre: жанры для фильмов
    - mpa_rating: возрастной рейтинг фильмов
- Связующие таблицы
  - user_friend: какие из других пользователей являются друзьями
  - user_film_likes: пользователи, которым понравился фильм
  - film_genre: жанры фильма

### SQL requests samples
[Sample SQL requests](/SQL_samles.sql)

