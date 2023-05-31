package ru.yandex.practicum.filmorate.repository.user.h2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.FriendConfirmationStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.OperationType;
import ru.yandex.practicum.filmorate.repository.feed.FeedStorage;
import ru.yandex.practicum.filmorate.repository.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@Qualifier("H2UserRepository")
@Slf4j
@RequiredArgsConstructor
public class UserRepository implements UserStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final FeedStorage feedStorage;

    @Override
    public User addUser(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Long userId = simpleJdbcInsert.executeAndReturnKey(user.mapToDb()).longValue();

        user.setId(userId);
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sqlQuery = "UPDATE users " +
                "SET user_name = :userName, login = :login, email = :email, birthday = :birthday " +
                "WHERE user_id = :userId";
        MapSqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userName", user.getName())
                .addValue("login", user.getLogin())
                .addValue("email", user.getEmail())
                .addValue("birthday", user.getBirthday())
                .addValue("userId", user.getId());

        jdbcTemplate.update(sqlQuery, namedParams);

        return user;
    }

    @Override
    public List<User> getAllUsers() {
        String sqlQuery = "SELECT user_id, user_name, login, email, birthday " +
                "FROM users";

        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        String sqlQuery = "SELECT user_id, user_name, login, email, birthday " +
                "FROM users " +
                "WHERE user_id = :userId";
        SqlParameterSource namedParam = new MapSqlParameterSource("userId", id);
        Optional<User> user;

        try {
            user = Optional.ofNullable(
                    jdbcTemplate.queryForObject(sqlQuery, namedParam, this::mapRowToUser)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
        return user;
    }

    @Override
    @Transactional
    public void addFriendToUser(Long userId, Long friendId) {
        String sqlQueryAddUserRecord = "MERGE INTO user_friend (user_id, friend_id, confirmation_status) " +
                "VALUES (:userId, :friendId, :confirmed)";
        String sqlQueryAddFriendRecord = "MERGE INTO user_friend (user_id, friend_id, confirmation_status) " +
                "VALUES (:friendId, :userId, :pending)";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId)
                .addValue("confirmed", FriendConfirmationStatus.CONFIRMED.toString())
                .addValue("pending", FriendConfirmationStatus.WAITING_FOR_APPROVAL.toString());

        jdbcTemplate.update(sqlQueryAddUserRecord, namedParams);
        jdbcTemplate.update(sqlQueryAddFriendRecord, namedParams);
        feedStorage.addEvent(userId, friendId, EventType.FRIEND, OperationType.ADD);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        String sqlQuery = "SELECT user_id, user_name, login, email, birthday " +
                "FROM users " +
                "WHERE user_id IN " +
                "(SELECT friend_id FROM " +
                "(SELECT user_id, friend_id " +
                "FROM user_friend " +
                "WHERE user_id = :userId AND confirmation_status = :confirmed " +
                "UNION ALL " +
                "SELECT user_id, friend_id " +
                "FROM user_friend " +
                "WHERE user_id = :otherUserId AND confirmation_status = :confirmed) " +
                "GROUP BY FRIEND_ID " +
                "HAVING COUNT(USER_ID) > 1)";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("otherUserId", otherUserId)
                .addValue("confirmed", FriendConfirmationStatus.CONFIRMED.toString());

        try {
            return jdbcTemplate.query(sqlQuery, namedParams, this::mapRowToUser);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    @Override
    public List<User> getUserFriends(Long userId) {
        String sqlQuery = "SELECT user_id, user_name, login, email, birthday " +
                "FROM users " +
                "WHERE user_id IN " +
                "(SELECT friend_id " +
                "FROM user_friend " +
                "WHERE user_id = :userId AND confirmation_status = :confirmed)";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("confirmed", FriendConfirmationStatus.CONFIRMED.toString());

        try {
            return jdbcTemplate.query(sqlQuery, namedParams, this::mapRowToUser);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    @Override
    public boolean removeFriendFromUser(Long userId, Long friendId) {
        //Если друг не добавлял пользователя в друзья (статус WAITING_FOR_APPROVAL)
        String deleteBothRecordsSqlQuery = "DELETE FROM user_friend " +
                "WHERE (user_id = :userId AND friend_id = :friendId)" +
                "OR " +
                "(user_id = :friendId AND friend_id = :userId)";
        //Если друг добавил пользователя в друзья (статус CONFIRMED)
        String changeFriendStatusForPending = "UPDATE user_friend " +
                "SET confirmation_status = :pending " +
                "WHERE user_id = :userId AND friend_id= :friendId";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId)
                .addValue("pending", FriendConfirmationStatus.WAITING_FOR_APPROVAL.toString());

        Optional<FriendConfirmationStatus> currentUserFriendStatus = getFriendshipStatus(userId, friendId);
        Optional<FriendConfirmationStatus> currentFriendUserStatus = getFriendshipStatus(friendId, userId);

        if (
                (currentUserFriendStatus.isEmpty() || currentFriendUserStatus.isEmpty()) ||
                        currentUserFriendStatus.get().equals(FriendConfirmationStatus.WAITING_FOR_APPROVAL)
        ) {
            return false;
        }

        if (currentFriendUserStatus.get().equals(FriendConfirmationStatus.CONFIRMED) &&
                currentUserFriendStatus.get().equals(FriendConfirmationStatus.CONFIRMED)) {
            feedStorage.addEvent(userId, friendId, EventType.FRIEND, OperationType.REMOVE);
            return jdbcTemplate.update(changeFriendStatusForPending, namedParams) > 0;
        }

        if (currentUserFriendStatus.get().equals(FriendConfirmationStatus.CONFIRMED) &&
                currentFriendUserStatus.get().equals(FriendConfirmationStatus.WAITING_FOR_APPROVAL)) {
            feedStorage.addEvent(userId, friendId, EventType.FRIEND, OperationType.REMOVE);
            return jdbcTemplate.update(deleteBothRecordsSqlQuery, namedParams) > 0;
        }

        return false;
    }

    @Override
    public void removeUserById(Long userId) {
        String sqlQuery = "DELETE FROM users " +
                "WHERE user_id = :userId";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId);

        jdbcTemplate.update(sqlQuery, namedParams);

    }

    private Optional<FriendConfirmationStatus> getFriendshipStatus(Long userId, Long otherUserId) {
        String sqlQuery = "SELECT confirmation_status " +
                "FROM user_friend " +
                "WHERE (user_id = :userId AND friend_id = :otherUserId)";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("otherUserId", otherUserId);
        String status;

        try {
            status = jdbcTemplate.queryForObject(sqlQuery, namedParams, String.class);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

        FriendConfirmationStatus enumStatus = FriendConfirmationStatus.valueOf(status);

        return Optional.of(enumStatus);
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("user_id"))
                .name(resultSet.getString("user_name"))
                .login(resultSet.getString("login"))
                .email(resultSet.getString("email"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
    }
}
