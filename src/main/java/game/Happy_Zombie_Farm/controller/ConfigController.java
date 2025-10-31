package game.Happy_Zombie_Farm.controller;

import game.Happy_Zombie_Farm.config.GameLogicCfg;
import game.Happy_Zombie_Farm.config.HousesInfoCfg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private HousesInfoCfg housesInfoCfg;

    @Autowired
    private GameLogicCfg gameLogicCfg;

    @GetMapping("/houses-info")
    public ResponseEntity<HousesInfoCfg> getHouseInfoConfig() {
        return ResponseEntity.ok().body(housesInfoCfg);
    }

    @GetMapping("/game-logic")
    public ResponseEntity<GameLogicCfg> getGameLogicConfig() {
        return ResponseEntity.ok().body(gameLogicCfg);
    }

}
