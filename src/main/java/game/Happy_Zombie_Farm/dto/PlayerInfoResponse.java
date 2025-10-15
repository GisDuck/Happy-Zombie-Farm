package game.Happy_Zombie_Farm.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record PlayerInfoResponse (
    Long telegramId,
    String username,
    String photoUrl,
    Long brains,
    Long money,
    Integer brainsPerMinute,
    Integer moneyPerMinute,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String occupiedCells,
    List<PlayerBuildingDto> buildings
) {}
