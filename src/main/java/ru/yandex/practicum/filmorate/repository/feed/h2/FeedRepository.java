package ru.yandex.practicum.filmorate.repository.feed.h2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.feed.*;
import ru.yandex.practicum.filmorate.repository.feed.FeedStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class FeedRepository implements FeedStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Feed feed) {
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
        return jdbcTemplate.query(sqlQuery, namedParams, this::mapRowToFeed);
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