package game.Happy_Zombie_Farm.mapper;

import game.Happy_Zombie_Farm.dto.PlayerDto;
import game.Happy_Zombie_Farm.entity.Player;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {HouseMapper.class})
public interface PlayerMapper {

    PlayerDto toDto(Player entity);
}
