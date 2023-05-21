package ru.yandex.practicum.filmorate.model.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.HashMap;
import java.util.Map;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Feed {
    private Long timestamp;
    @PositiveOrZero
    private Long userId;
    @NotNull
    private EventType eventType;
    @NotNull
    private OperationType operation;
    @PositiveOrZero
    private Long eventId;
    @PositiveOrZero
    private Long entityId;

    public Map<String, Object> mapToDb() {
        Map<String, Object> feedValues = new HashMap<>();
        feedValues.put("timestamp", timestamp);
        feedValues.put("userId", userId);
        feedValues.put("eventType", eventType.toString());
        feedValues.put("Operation", operation.toString());
        feedValues.put("entityId", entityId);
        return feedValues;
    }
}
