package game.Happy_Zombie_Farm.service;

import game.Happy_Zombie_Farm.dto.PlayerBuildingDto;
import game.Happy_Zombie_Farm.dto.PlayerInfoRequest;
import game.Happy_Zombie_Farm.dto.PlayerInfoResponse;
import game.Happy_Zombie_Farm.exception.NoPlayerException;
import game.Happy_Zombie_Farm.model.Player;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerBoardService playerBoardService;
    private final PlayerBuildingService playerBuildingService;

    @Value("${START_BRAIN:}")
    private Long startBrain;

    @Value("${START_MONEY:}")
    private Long startMoney;

    public PlayerInfoResponse getPlayerInfoResponse(Integer telegramId) {
        Player player = playerRepository.findByTelegramId(telegramId)
            .orElseThrow(() -> new NoPlayerException(telegramId));

        List<PlayerBuildingDto> buildingDtos = playerBuildingService.entitysToDtos(player.getBuildings());

        return new PlayerInfoResponse(
                telegramId,
                player.getUsername(),
                player.getPhotoUrl(),
                player.getBrains(),
                player.getMoney(),
                player.getBrainsPerMinute(),
                player.getMoneyPerMinute(),
                player.getCreatedAt(),
                player.getUpdatedAt(),
                player.getBoard().getOccupiedCells(),
                buildingDtos
        );
    }

    public PlayerInfoResponse createNewPlayer(PlayerInfoRequest playerInfoRequest) {
        Player player = new Player();
        player.setTelegramId(playerInfoRequest.getTelegramId());
        player.setPhotoUrl(playerInfoRequest.getPhotoUrl());
        player.setUsername(player.getUsername());
        player.setBrains(startBrain);
        player.setMoney(startMoney);

        playerRepository.save(player);

        return new PlayerInfoResponse(
                player.getTelegramId(),
                player.getUsername(),
                player.getPhotoUrl(),
                player.getBrains(),
                player.getMoney(),
                player.getBrainsPerMinute(),
                player.getMoneyPerMinute(),
                player.getCreatedAt(),
                player.getUpdatedAt(),
                player.getBoard().getOccupiedCells(),
                List.of()
        );
    }
}