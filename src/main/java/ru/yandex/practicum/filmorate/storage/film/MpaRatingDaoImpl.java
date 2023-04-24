package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.WrongMpaRatingException;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaRatingDaoImpl implements MpaRatingDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public RatingMPA getMpaByIdFromDb(int mpaId) {
        if (mpaId == 0) {
            return null;
        }

        String getMpaNameSqlQuery = "SELECT MPA_RATING_NAME FROM MPA_RATING " +
                "WHERE MPA_RATING_ID = :mpaRatingId";
        SqlParameterSource namedParam = new MapSqlParameterSource().addValue("mpaRatingId", mpaId);

        Optional<String> mpaName = Optional.ofNullable(
                jdbcTemplate.queryForObject(getMpaNameSqlQuery, namedParam, String.class)
        );

        return new RatingMPA(
                mpaId,
                mpaName.orElseThrow(() -> new WrongMpaRatingException(
                        String.format("There is no Mpa Rating for id: %n", mpaId))
                )
        );
    }
}
