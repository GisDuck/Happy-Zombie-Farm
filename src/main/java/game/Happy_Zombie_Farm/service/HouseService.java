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
import game.Happy_Zombie_Farm.mapper.HouseMapper;
import game.Happy_Zombie_Farm.repository.HouseRepository;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public HouseDto buildHouse(BuildHouseInputDto input) throws NoPlayerException {
        Long playerId = 0L; //!!!ДОСТАТЬ ИЗ СЕКЬЮРИТИ!!!

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));

        Long gold = housesInfoCfg
            .type()
            .get(input.type())
            .skins()
            .get(input.skin())
            .price();

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
    public HouseDto updateHouseLevel(HouseIdInputDto input)
            throws NoHouseException,
            NotThisPlayerHouseIdException
    {
        Long playerId = 0L; //!!!ДОСТАТЬ ИЗ СЕКЬЮРИТИ!!!

        House house = houseRepository.findById(input.houseId())
                .orElseThrow(() -> new NoHouseException(input.houseId()));

        Player player = house.getPlayer();

        if (player.getId() != playerId) {
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
    public HouseDto updateHouseSkin(UpdateHouseSkinInputDto input)
            throws NoHouseException,
            NotThisPlayerHouseIdException
    {
        Long playerId = 0L; //!!!ДОСТАТЬ ИЗ СЕКЬЮРИТИ!!!

        House house = houseRepository.findById(input.houseId())
                .orElseThrow(() -> new NoHouseException(input.houseId()));

        Player player = house.getPlayer();

        if (player.getId() != playerId) {
            throw new NotThisPlayerHouseIdException(input.houseId());
        }

        Long newSkinPrice = housesInfoCfg
            .type()
            .get(house.getType())
            .skins()
            .get(input.newSkin())
            .price();

        playerService.takeMoney(newSkinPrice, player);

        house.setSkin(input.newSkin());
        house = houseRepository.save(house);
        return houseMapper.toDto(house);
    }

    @Transactional
    public HouseDto updateHouseLocation(UpdateHouseLocationInputDto input)
            throws NoHouseException,
            NotThisPlayerHouseIdException
    {
        Long playerId = 0L; //!!!ДОСТАТЬ ИЗ СЕКЬЮРИТИ!!!

        House house = houseRepository.findById(input.houseId())
                .orElseThrow(() -> new NoHouseException(input.houseId()));

        Player player = house.getPlayer();

        if (player.getId() != playerId) {
            throw new NotThisPlayerHouseIdException(input.houseId());
        }

        house.setLocationX(input.newLocationX());
        house.setLocationY(input.newLocationY());
        house = houseRepository.save(house);
        return houseMapper.toDto(house);
    }

    @Transactional
    public RemoveHousePayloadDto deleteHouse(HouseIdInputDto input) throws NoHouseException {
        Long playerId = 0L; //!!!ДОСТАТЬ ИЗ СЕКЬЮРИТИ!!!

        House house = houseRepository.findById(input.houseId())
                .orElseThrow(() -> new NoHouseException(input.houseId()));

        Player player = house.getPlayer();

        if (player.getId() != playerId) {
            throw new NotThisPlayerHouseIdException(input.houseId());
        }

        Long gold = housesInfoCfg
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

