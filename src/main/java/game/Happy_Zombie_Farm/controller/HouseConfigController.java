package game.Happy_Zombie_Farm.controller;

import game.Happy_Zombie_Farm.config.HouseInfoCfg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class HouseConfigController {

    @Autowired
    private HouseInfoCfg houseInfoCfg;

    /**
     * GET /api/config/house
     * Отдаёт JSON с той же структурой, что и в YAML **/
    @GetMapping("/houses")
    public ResponseEntity<HouseInfoCfg> getHouseConfig() {
        return ResponseEntity.ok().body(houseInfoCfg);
    }
}
