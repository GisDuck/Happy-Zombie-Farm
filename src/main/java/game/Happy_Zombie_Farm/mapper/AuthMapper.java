package game.Happy_Zombie_Farm.mapper;

import game.Happy_Zombie_Farm.dto.outputDto.AuthPayloadDto;
import game.Happy_Zombie_Farm.dto.outputDto.PlayerDto;
import game.Happy_Zombie_Farm.entity.Player;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PlayerMapper.class})
public interface AuthMapper {

    default AuthPayloadDto toDto(Player player, String token) {
        return new AuthPayloadDto(
                toPlayerDto(player),
                token
        );
    }

    PlayerDto toPlayerDto(Player player);
}

