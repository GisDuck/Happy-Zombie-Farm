package game.Happy_Zombie_Farm.dto.outputDto;

public record AuthPayloadDto(
        PlayerDto player,
        String accessToken
) {}
