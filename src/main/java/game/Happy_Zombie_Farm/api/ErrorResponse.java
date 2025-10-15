package game.Happy_Zombie_Farm.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String status,        // "error"
        int code,             // 404, 409, 500
        String message,       // Ошибка со стороны сервера
        String path,          // /api/game/player-info
        OffsetDateTime timestamp
) {
    public static ErrorResponse of(int code, String message, String path) {
        return new ErrorResponse("error", code, message, path, OffsetDateTime.now());
    }
}

