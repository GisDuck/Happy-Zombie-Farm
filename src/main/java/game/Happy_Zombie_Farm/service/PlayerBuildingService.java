package game.Happy_Zombie_Farm.service;

import game.Happy_Zombie_Farm.dto.PlayerBuildingDto;
import game.Happy_Zombie_Farm.model.Player;
import game.Happy_Zombie_Farm.model.PlayerBuilding;
import game.Happy_Zombie_Farm.repository.PlayerBuildingRepository;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerBuildingService {
    private final PlayerRepository playerRepository;
    private final PlayerBuildingRepository playerBuildingRepository;

    public List<PlayerBuildingDto> entitysToDtos(List<PlayerBuilding> buildings) {
        return buildings
                .stream()
                .map(PlayerBuildingService::entityToDto)
                .toList();
    }

    public static PlayerBuildingDto entityToDto(PlayerBuilding building) {
        return new PlayerBuildingDto(
            building.getPlayer().getTelegramId(),
            building.getBuildingType().getCode(),
            building.getOriginX(),
            building.getOriginY(),
            building.getPlacedAt()
        );
    }


}
