package game.Happy_Zombie_Farm.dto.inputDto;

import game.Happy_Zombie_Farm.enums.HouseType;

// BuildHouseInput
public record BuildHouseInputDto(
        HouseType type,
        Integer skin,
        Integer locationX,
        Integer locationY
) {}
