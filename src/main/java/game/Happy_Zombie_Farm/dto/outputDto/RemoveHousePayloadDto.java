package game.Happy_Zombie_Farm.dto.outputDto;

public record RemoveHousePayloadDto(
        boolean success,
        Long deletedHouseId
) {}

