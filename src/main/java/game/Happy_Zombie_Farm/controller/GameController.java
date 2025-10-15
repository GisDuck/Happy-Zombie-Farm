package game.Happy_Zombie_Farm.controller;

import game.Happy_Zombie_Farm.dto.BuildRequest;
import game.Happy_Zombie_Farm.dto.PlayerInfoRequest;
import game.Happy_Zombie_Farm.dto.PlayerInfoResponse;
import game.Happy_Zombie_Farm.exception.NoPlayerException;
import game.Happy_Zombie_Farm.model.Player;
import game.Happy_Zombie_Farm.model.PlayerBoard;
import game.Happy_Zombie_Farm.model.PlayerBuilding;
import game.Happy_Zombie_Farm.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@Validated
public class GameController {
    private final PlayerService playerService;
    private final PlayerBoardService boardService;
    private final PlayerBuildingService buildingService;

    private static final Logger log = LoggerFactory.getLogger(GameController.class);


    @PostMapping("/player-info")
    public ResponseEntity<PlayerInfoResponse> playerInfo(@RequestBody PlayerInfoRequest req) {
        log.info("req", req);

        try {
            var resp = playerService.getPlayerInfoResponse(req.getTelegramId());
            return ResponseEntity.ok(resp);
        } catch (NoPlayerException e) {
            var created = playerService.createNewPlayer(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
    }
}
