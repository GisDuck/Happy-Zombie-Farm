package game.Happy_Zombie_Farm.dto.inputDto;

public record UpdateHouseLocationInputDto(
        Long houseId,
        Integer newLocationX,
        Integer newLocationY
) {}
