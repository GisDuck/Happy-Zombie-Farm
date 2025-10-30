package game.Happy_Zombie_Farm.dto.outputDto;

public record DeleteHousePayloadDto(
        boolean success,
        Long deletedHouseId
) {}

