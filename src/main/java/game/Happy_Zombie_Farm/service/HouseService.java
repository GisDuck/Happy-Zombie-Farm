package game.Happy_Zombie_Farm.service;

import game.Happy_Zombie_Farm.config.GameLogicCfg;
import game.Happy_Zombie_Farm.config.HousesInfoCfg;
import game.Happy_Zombie_Farm.dto.inputDto.*;
import game.Happy_Zombie_Farm.dto.outputDto.RemoveHousePayloadDto;
import game.Happy_Zombie_Farm.dto.HouseDto;
import game.Happy_Zombie_Farm.entity.House;
import game.Happy_Zombie_Farm.entity.Player;
import game.Happy_Zombie_Farm.exception.NoHouseException;
import game.Happy_Zombie_Farm.exception.NoPlayerException;
import game.Happy_Zombie_Farm.exception.NotThisPlayerHouseIdException;
import game.Happy_Zombie_Farm.exception.WrongSkinHouseParamException;
import game.Happy_Zombie_Farm.mapper.HouseMapper;
import game.Happy_Zombie_Farm.repository.HouseRepository;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HouseService {
    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private HousesInfoCfg housesInfoCfg;
    @Autowired
    private GameLogicCfg gameLogicCfg;

    public HouseDto getHouseDtoById(Long houseId) {
        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new NoHouseException(houseId));
        return houseMapper.toDto(house);
    }

    public List<HouseDto> getPlayerHousesDto(Long playerId) {
        List<House> houses = houseRepository.findByPlayerId(playerId);
        return houses.stream()
            .map(houseMapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public HouseDto buildHouse(Long playerId, BuildHouseInputDto input) throws NoPlayerException {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));

        log.info("BuildHouseInputDto = {} ", input);

        Map<String, HousesInfoCfg.SkinCfg> skins = housesInfoCfg
                .type()
                .get(input.type())
                .skins();

        HousesInfoCfg.SkinCfg skinCfg = skins.get(input.skin());

        if (skinCfg == null) {
            log.info("Unknown skin for build '{}', for: {}", input.skin(), skins.keySet());
            throw new WrongSkinHouseParamException(input.type().name(), input.skin());
        }

        Long gold = skinCfg.price();

        playerService.takeMoney(gold, player);

        House house = new House();
        house.setPlayer(player);
        house.setType(input.type());
        house.setLevel(0);
        house.setSkin(input.skin());
        house.setLocationX(input.locationX());
        house.setLocationY(input.locationY());

        house = houseRepository.save(house);
        return houseMapper.toDto(house);
    }

    @Transactional
    public HouseDto updateHouseLevel(Long playerId, HouseIdInputDto input)
            throws NoHouseException,
            NotThisPlayerHouseIdException
    {
        House house = houseRepository.findById(input.houseId())
                .orElseThrow(() -> new NoHouseException(input.houseId()));

        Player player = house.getPlayer();

        if (!Objects.equals(player.getId(), playerId)) {
            throw new NotThisPlayerHouseIdException(input.houseId());
        }

        Long gold = housesInfoCfg
            .type()
            .get(house.getType())
            .levels()
            .get(house.getLevel() + 1)
            .price();

        playerService.takeMoney(gold, player);

        house.setLevel(house.getLevel() + 1);
        house = houseRepository.save(house);
        return houseMapper.toDto(house);
    }

    @Transactional
    public HouseDto updateHouseSkin(Long playerId, UpdateHouseSkinInputDto input)
            throws NoHouseException,
            NotThisPlayerHouseIdException
    {
        House house = houseRepository.findById(input.houseId())
                .orElseThrow(() -> new NoHouseException(input.houseId()));

        Player player = house.getPlayer();

        if (!Objects.equals(player.getId(), playerId)) {
            throw new NotThisPlayerHouseIdException(input.houseId());
        }

        log.info("UpdateHouseSkinInputDto = {} ", input);

        Map<String, HousesInfoCfg.SkinCfg> skins = housesInfoCfg
                .type()
                .get(house.getType())
                .skins();

        HousesInfoCfg.SkinCfg skinCfg = skins.get(input.newSkin());

        if (skinCfg == null) {
            log.info("Unknown skin for update '{}', for: {}", input.newSkin(), skins.keySet());
            throw new WrongSkinHouseParamException(house.getType().name(), input.newSkin());
        }

        Long newSkinPrice = skinCfg.price();

        playerService.takeMoney(newSkinPrice, player);

        house.setSkin(input.newSkin());
        house = houseRepository.save(house);
        return houseMapper.toDto(house);
    }

    @Transactional
    public HouseDto updateHouseLocation(Long playerId, UpdateHouseLocationInputDto input)
            throws NoHouseException,
            NotThisPlayerHouseIdException
    {
        House house = houseRepository.findById(input.houseId())
                .orElseThrow(() -> new NoHouseException(input.houseId()));

        Player player = house.getPlayer();

        if (!Objects.equals(player.getId(), playerId)) {
            throw new NotThisPlayerHouseIdException(input.houseId());
        }

        house.setLocationX(input.newLocationX());
        house.setLocationY(input.newLocationY());
        house = houseRepository.save(house);
        return houseMapper.toDto(house);
    }

    @Transactional
    public RemoveHousePayloadDto removeHouse(Long playerId, HouseIdInputDto input) throws NoHouseException {
        House house = houseRepository.findById(input.houseId())
                .orElseThrow(() -> new NoHouseException(input.houseId()));

        Player player = house.getPlayer();

        if (!Objects.equals(player.getId(), playerId)) {
            throw new NotThisPlayerHouseIdException(input.houseId());
        }

        long gold = housesInfoCfg
                .type()
                .get(house.getType())
                .skins()
                .get(house.getSkin())
                .price();

        for (int lvl = house.getLevel(); lvl > 0; lvl--) {
            gold += housesInfoCfg
                    .type()
                    .get(house.getType())
                    .levels()
                    .get(lvl)
                    .price();
        }

        gold = (long) (gold * gameLogicCfg.returnGoldForHouse());

        playerService.returnMoney(gold, player);
        houseRepository.deleteById(input.houseId());
        return new RemoveHousePayloadDto(true, input.houseId());
    }
}

