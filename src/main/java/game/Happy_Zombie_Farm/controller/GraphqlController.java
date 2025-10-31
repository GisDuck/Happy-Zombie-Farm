package game.Happy_Zombie_Farm.controller;

import game.Happy_Zombie_Farm.config.GameLogicCfg;
import game.Happy_Zombie_Farm.config.HousesInfoCfg;
import game.Happy_Zombie_Farm.dto.HouseDto;
import game.Happy_Zombie_Farm.dto.PlayerDto;
import game.Happy_Zombie_Farm.dto.inputDto.*;
import game.Happy_Zombie_Farm.dto.outputDto.RemoveHousePayloadDto;
import game.Happy_Zombie_Farm.mapper.PlayerMapper;
import game.Happy_Zombie_Farm.service.HouseService;
import game.Happy_Zombie_Farm.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.Argument;

import java.util.List;

@Controller
public class GraphqlController {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private HouseService houseService;
    @Autowired
    private PlayerMapper playerMapper;
    @Autowired
    private GameLogicCfg gameLogicCfg;
    @Autowired
    private HousesInfoCfg housesInfoCfg;

    // ---- Queries ----
    @QueryMapping
    public PlayerDto getPlayer(@AuthenticationPrincipal(expression = "playerId") Long playerId) {
        return playerService.getPlayerDto(playerId);
    }

    @QueryMapping
    public HouseDto getHouse(@Argument Long houseId) {
        return houseService.getHouseDtoById(houseId);
    }

    @QueryMapping
    public List<HouseDto> getPlayerHouses(@AuthenticationPrincipal(expression = "playerId") Long playerId) {
        return houseService.getPlayerHousesDto(playerId);
    }

    @QueryMapping
    public GameLogicCfg getGameLogicCfg() {
        return gameLogicCfg;
    }

    @QueryMapping
    public HousesInfoCfg getHousesInfoCfg() {
        return housesInfoCfg;
    }

    // ---- Mutations ----
    @MutationMapping
    public HouseDto buildHouse(@AuthenticationPrincipal(expression = "playerId") Long playerId,
                               @Argument BuildHouseInputDto input) {
        return houseService.buildHouse(playerId, input);
    }

    @MutationMapping
    public HouseDto updateHouseSkin(@AuthenticationPrincipal(expression = "playerId") Long playerId,
                                    @Argument UpdateHouseSkinInputDto input) {
        return houseService.updateHouseSkin(playerId, input);
    }

    @MutationMapping
    public HouseDto updateHouseLocation(@AuthenticationPrincipal(expression = "playerId") Long playerId,
                                        @Argument UpdateHouseLocationInputDto input) {
        return houseService.updateHouseLocation(playerId, input);
    }

    @MutationMapping
    public HouseDto updateHouseLevel(@AuthenticationPrincipal(expression = "playerId") Long playerId,
                                     @Argument HouseIdInputDto input) {
        return houseService.updateHouseLevel(playerId, input);
    }

    @MutationMapping
    public RemoveHousePayloadDto removeHouse(@AuthenticationPrincipal(expression = "playerId") Long playerId,
                                             @Argument HouseIdInputDto input) {
        return houseService.removeHouse(playerId, input);
    }

    @MutationMapping
    public PlayerDto updatePlayerMeat(@AuthenticationPrincipal(expression = "playerId") Long playerId) {
        return playerMapper.toDto(playerService.updatePlayerMeat(playerId));
    }

    @MutationMapping
    public PlayerDto convertMeatToBrain(
            @AuthenticationPrincipal(expression = "playerId") Long playerId,
            @Argument ConvertMeatToBrainInputDto input
    ) {
        return playerService.convertMeatToBrain(playerId, input);
    }

    @MutationMapping
    public PlayerDto convertBrainToGold(
            @AuthenticationPrincipal(expression = "playerId") Long playerId,
            @Argument ConvertBrainToGoldInputDto input
    ) {
        return playerService.convertBrainToGold(playerId, input);
    }
}

