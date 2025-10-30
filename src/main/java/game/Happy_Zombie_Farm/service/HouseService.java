package game.Happy_Zombie_Farm.service;

import game.Happy_Zombie_Farm.config.HousesInfoCfg;
import game.Happy_Zombie_Farm.dto.inputDto.*;
import game.Happy_Zombie_Farm.dto.outputDto.DeleteHousePayloadDto;
import game.Happy_Zombie_Farm.dto.outputDto.HouseDto;
import game.Happy_Zombie_Farm.entity.House;
import game.Happy_Zombie_Farm.entity.Player;
import game.Happy_Zombie_Farm.exception.NoHouseException;
import game.Happy_Zombie_Farm.exception.NoPlayerException;
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

    @Transactional
    public HouseDto buildHouse(Long playerId, BuildHouseInputDto input) throws NoPlayerException {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));

        Long gold = housesInfoCfg.type().get(input.type()).skins().get(input.skin()).price();

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
    public HouseDto updateHouseLevel(HouseIdInputDto input) throws NoHouseException {

        //!!!!!!ДОБАВИТЬ ПРОВЕРКУ ДЕНЕГ!!!!!!!!

        House house = houseRepository.findById(input.houseId())
                .orElseThrow(() -> new NoHouseException(input.houseId()));
        house.setLevel(house.getLevel() + 1);
        house = houseRepository.save(house);
        return houseMapper.toDto(house);
    }

    @Transactional
    public HouseDto updateHouseSkin(UpdateHouseSkinInputDto input) throws NoHouseException {

        //!!!!!!ДОБАВИТЬ ПРОВЕРКУ ДЕНЕГ!!!!!!!!

        House house = houseRepository.findById(input.houseId())
                .orElseThrow(() -> new NoHouseException(input.houseId()));
        house.setSkin(input.newSkin());
        house = houseRepository.save(house);
        return houseMapper.toDto(house);
    }

    @Transactional
    public HouseDto updateHouseLocation(UpdateHouseLocationInputDto input) throws NoHouseException {
        House house = houseRepository.findById(input.houseId())
                .orElseThrow(() -> new NoHouseException(input.houseId()));
        house.setLocationX(input.newLocationX());
        house.setLocationY(input.newLocationY());
        house = houseRepository.save(house);
        return houseMapper.toDto(house);
    }

    @Transactional
    public DeleteHousePayloadDto deleteHouse(HouseIdInputDto input) throws NoHouseException {

        //!!!!!!ДОБАВИТЬ ВОЗВРАТ ДЕНЕГ!!!!!!!!

        if (houseRepository.existsById(input.houseId())) {
            houseRepository.deleteById(input.houseId());
        } else {
            throw new NoHouseException(input.houseId());
        }
        return new DeleteHousePayloadDto(true, input.houseId());
    }
}

