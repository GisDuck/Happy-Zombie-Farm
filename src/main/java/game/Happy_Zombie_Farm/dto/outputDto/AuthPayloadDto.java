package game.Happy_Zombie_Farm.dto.outputDto;

import game.Happy_Zombie_Farm.dto.PlayerDto;

public record AuthPayloadDto(
        PlayerDto player,
        String accessToken
) {}
