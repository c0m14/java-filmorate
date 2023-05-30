package ru.yandex.practicum.filmorate.repository.feed.h2;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.feed.*;
import ru.yandex.practicum.filmorate.repository.feed.FeedStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FeedRepository implements FeedStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Long userId, Long entityId, EventType eventType, OperationType operationType) {
        Feed feed = Feed.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(eventType)
                .operation(operationType)
                .entityId(entityId)
                .build();
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("feed")
                .usingGeneratedKeyColumns("eventId");
        simpleJdbcInsert.executeAndReturnKey(feed.mapToDb()).longValue();
    }

    @Override
    public List<Feed> getUserFeed(Long userId) {
        String sqlQuery = "SELECT timestamp, userId, eventType, operation, eventId, entityId " +
                "FROM feed " +
                "WHERE userId = :userId";
        SqlParameterSource namedParams = new MapSqlParameterSource()
                .addValue("userId", userId);
        try {
            return jdbcTemplate.query(sqlQuery, namedParams, this::mapRowToFeed);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    private Feed mapRowToFeed(ResultSet resultSet, int rowNum) throws SQLException {
        return Feed.builder()
                .timestamp(resultSet.getLong("timestamp"))
                .userId(resultSet.getLong("userId"))
                .eventType(EventType.valueOf(resultSet.getString("eventType")))
                .operation(OperationType.valueOf(resultSet.getString("operation")))
                .entityId(resultSet.getLong("entityId"))
                .eventId(resultSet.getLong("eventId"))
                .build();
    }
}