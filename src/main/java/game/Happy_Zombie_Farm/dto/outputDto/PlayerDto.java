package game.Happy_Zombie_Farm.dto.outputDto;

import game.Happy_Zombie_Farm.enums.BoardColor;
import java.util.List;

public record PlayerDto(
        Long id,
        String username,
        Long meat,
        Long gold,
        Long brain,
        BoardColor boardColor,
        List<HouseDto> houses
) {}

