package game.Happy_Zombie_Farm.mapper;

import game.Happy_Zombie_Farm.dto.HouseDto;
import game.Happy_Zombie_Farm.entity.House;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HouseMapper {

    @Mapping(target = "playerId", source = "player.id")
    HouseDto toDto(House entity);

    // если нужно из dto в entity (редко надо)
    @InheritInverseConfiguration
    @Mapping(target = "player", ignore = true)
    House toEntity(HouseDto dto);
}

