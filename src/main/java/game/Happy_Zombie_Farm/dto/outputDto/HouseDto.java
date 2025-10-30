package game.Happy_Zombie_Farm.dto.outputDto;

import game.Happy_Zombie_Farm.enums.HouseType;

public record HouseDto(
        Long id,
        Long playerId,
        HouseType type,
        Integer level,
        Integer skin,
        Integer locationX,
        Integer locationY
) {}
