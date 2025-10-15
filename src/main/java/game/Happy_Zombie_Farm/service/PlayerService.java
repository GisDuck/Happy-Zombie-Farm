package game.Happy_Zombie_Farm.service;

import game.Happy_Zombie_Farm.controller.TelegramAuthController;
import game.Happy_Zombie_Farm.dto.PlayerBuildingDto;
import game.Happy_Zombie_Farm.dto.PlayerInfoRequest;
import game.Happy_Zombie_Farm.dto.PlayerInfoResponse;
import game.Happy_Zombie_Farm.exception.NoPlayerException;
import game.Happy_Zombie_Farm.model.Player;
import game.Happy_Zombie_Farm.model.PlayerBoard;
import game.Happy_Zombie_Farm.repository.PlayerBoardRepository;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final PlayerBoardRepository playerBoardRepository;
    private final PlayerBoardService playerBoardService;
    private final PlayerBuildingService playerBuildingService;

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    @Value("${START_BRAIN:}")
    private Long startBrain;

    @Value("${START_MONEY:}")
    private Long startMoney;

    public PlayerInfoResponse getPlayerInfoResponse(Long telegramId) {

        log.info("telegramId", telegramId.toString());

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

    @Transactional
    public PlayerInfoResponse createNewPlayer(PlayerInfoRequest req) {
        log.info("reg", req.toString());

        Player player = new Player();
        player.setTelegramId(req.getTelegramId());
        player.setPhotoUrl(req.getPhotoUrl());
        player.setUsername(player.getUsername());
        player.setBrains(startBrain);
        player.setMoney(startMoney);

        log.info("player", player.toString());

        PlayerBoard board = new PlayerBoard();
        board.setPlayer(player);
        board.setOccupiedCells("0".repeat(32 * 32));
        playerBoardRepository.save(board);

        player.setBoard(board);

        log.info("board", board.toString());

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