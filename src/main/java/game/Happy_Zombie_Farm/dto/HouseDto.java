package game.Happy_Zombie_Farm.dto;

import game.Happy_Zombie_Farm.enums.HouseType;

public record HouseDto(
        Long id,
        Long playerId,
        HouseType type,
        Integer level,
        String skin,
        Integer locationX,
        Integer locationY
) {}
