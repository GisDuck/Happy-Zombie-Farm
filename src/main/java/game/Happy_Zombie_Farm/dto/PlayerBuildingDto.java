package game.Happy_Zombie_Farm.dto;

import java.time.OffsetDateTime;

public record PlayerBuildingDto (
    Integer playerId,
    String buildingCode,
    Short originX,
    Short originY,
    OffsetDateTime placedAt
) {}