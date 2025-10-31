package game.Happy_Zombie_Farm.mapper;

import game.Happy_Zombie_Farm.dto.PlayerDto;
import game.Happy_Zombie_Farm.entity.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {HouseMapper.class})
public interface PlayerMapper {

    @Mapping(target = "meat",
            expression = "java(entity.getMeat() != null ? entity.getMeat().toString() : null)")
    @Mapping(target = "gold",
            expression = "java(entity.getGold() != null ? entity.getGold().toString() : null)")
    @Mapping(target = "brain",
            expression = "java(entity.getBrain() != null ? entity.getBrain().toString() : null)")
    PlayerDto toDto(Player entity);
}
