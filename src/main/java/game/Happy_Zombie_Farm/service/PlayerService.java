package game.Happy_Zombie_Farm.service;

import game.Happy_Zombie_Farm.config.GameLogicCfg;
import game.Happy_Zombie_Farm.config.HousesInfoCfg;
import game.Happy_Zombie_Farm.dto.PlayerDto;
import game.Happy_Zombie_Farm.dto.inputDto.ConvertBrainToGoldInputDto;
import game.Happy_Zombie_Farm.dto.inputDto.ConvertMeatToBrainInputDto;
import game.Happy_Zombie_Farm.entity.House;
import game.Happy_Zombie_Farm.entity.Player;
import game.Happy_Zombie_Farm.enums.HouseType;
import game.Happy_Zombie_Farm.exception.NoPlayerException;
import game.Happy_Zombie_Farm.exception.ResourcesException;
import game.Happy_Zombie_Farm.mapper.PlayerMapper;
import game.Happy_Zombie_Farm.repository.HouseRepository;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static graphql.com.google.common.primitives.Longs.max;

@Service
public class PlayerService {
    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerMapper playerMapper;
    @Autowired
    private HousesInfoCfg housesInfoCfg;
    @Autowired
    private GameLogicCfg gameLogicCfg;

    public PlayerDto getPlayerDto(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));
        return playerMapper.toDto(player);
    }

    @Transactional
    public Player updatePlayerMeat(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));

        return updatePlayerMeat(player);
    }

    @Transactional
    public Player updatePlayerMeat(Player player) {
        Instant now = Instant.now();
        Instant last = player.getLastMeatUpdate();

        long seconds = Duration.between(last, now).getSeconds();
        if (seconds <= 0) {
            return player;
        }

        List<House> farms = houseRepository.findByPlayerIdAndType(player.getId(), HouseType.FARM);
        List<House> storages = houseRepository.findByPlayerIdAndType(player.getId(), HouseType.STORAGE);
        long meatPerOneCowPerSec = gameLogicCfg.production().meatPerCowPerSec();

        long meatPerSecondByPlayer = farms.stream()
                .mapToLong(
                    h -> housesInfoCfg
                    .type()
                    .get(HouseType.FARM)
                    .levels()
                    .get(h.getLevel())
                    .cows()
                )
                .sum();

        long maxMeatStorage = storages.stream()
                .mapToLong(
                    h -> housesInfoCfg
                    .type()
                    .get(HouseType.STORAGE)
                    .levels()
                    .get(h.getLevel())
                    .maxMeat()
                )
                .sum();

        long meatToAdd = seconds * meatPerSecondByPlayer;
        long allPlayerMeat = player.getMeat() + meatToAdd;


        player.setMeat(max(maxMeatStorage, allPlayerMeat));

        // обновляем время последнего перерасчёта
        player.setLastMeatUpdate(now);

        return playerRepository.save(player);
    }

    @Transactional
    public void takeMoney(Long gold, Player player) throws ResourcesException {
        if (gold <= 0) {
            throw new ResourcesException("Gold must be positive for take");
        }
        if (player.getGold() >= gold) {
            updatePlayerMeat(player);
            player.setGold(player.getGold() - gold);
            return;
        }
        throw new ResourcesException(new StringBuilder()
                .append("NotEnough gold id=")
                .append(player.getId())
                .append(" hasGold=")
                .append(player.getGold())
                .append(" needGold=")
                .append(gold)
                .toString()
                );
    }

    @Transactional
    public void takeMoney(Long gold, Long playerId) throws ResourcesException, NoPlayerException {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));

        takeMoney(gold, player);
    }

    @Transactional
    public void returnMoney(Long gold, Player player) {
        if (gold <= 0) {
            throw new ResourcesException("Gold must be positive for return");
        }

        updatePlayerMeat(player);
        player.setGold(player.getGold() + gold);
    }

    @Transactional
    public void returnMoney(Long gold, Long playerId) throws NoPlayerException {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));

        returnMoney(gold, player);
    }

    @Transactional
    public PlayerDto convertMeatToBrain(Player player, ConvertMeatToBrainInputDto input) {
        long rate = gameLogicCfg.conversion().meatPerOneBrain();
        long meatToSpend = input.meatToSpend();

        updatePlayerMeat(player);

        if (meatToSpend <= 0) {
            throw new ResourcesException("meatToSpend must be > 0");
        }
        if (player.getMeat() < meatToSpend) {
            throw new ResourcesException("Not enough meat");
        }

        long actualBrain = meatToSpend / rate;

        player.setMeat(player.getMeat() - meatToSpend);
        player.setBrain(player.getBrain() + actualBrain);

        playerRepository.save(player);
        return playerMapper.toDto(player);
    }

    @Transactional
    public PlayerDto convertMeatToBrain(Long playerId, ConvertMeatToBrainInputDto input) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));

        return convertMeatToBrain(player, input);
    }

    @Transactional
    public PlayerDto convertBrainToGold(Player player, ConvertBrainToGoldInputDto input) {
        long rate = gameLogicCfg.conversion().brainPerOneCoin();
        long brainToSpend = input.brainToSpend();

        updatePlayerMeat(player);

        if (brainToSpend <= 0) {
            throw new ResourcesException("brainToSpend must be > 0");
        }
        if (player.getBrain() < brainToSpend) {
            throw new ResourcesException("Not enough brain");
        }

        long actualGold = brainToSpend / rate;

        player.setBrain(player.getBrain() - brainToSpend);
        player.setGold(player.getGold() + actualGold);

        playerRepository.save(player);
        return playerMapper.toDto(player);
    }

    @Transactional
    public PlayerDto convertBrainToGold(Long playerId, ConvertBrainToGoldInputDto input) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));

        return convertBrainToGold(player, input);
    }


}

