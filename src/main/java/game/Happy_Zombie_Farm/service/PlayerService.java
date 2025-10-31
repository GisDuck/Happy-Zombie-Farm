package game.Happy_Zombie_Farm.service;

import game.Happy_Zombie_Farm.dto.PlayerDto;
import game.Happy_Zombie_Farm.entity.Player;
import game.Happy_Zombie_Farm.exception.NoPlayerException;
import game.Happy_Zombie_Farm.exception.NotEnoughGoldException;
import game.Happy_Zombie_Farm.mapper.PlayerMapper;
import game.Happy_Zombie_Farm.repository.HouseRepository;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {
    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerMapper playerMapper;

    public PlayerDto getPlayerDto(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));
        return playerMapper.toDto(player);
    }

    public boolean isEnoughGold(Long gold, Player player) throws NoPlayerException {
        if (player == null) {
            throw new NoPlayerException("player is null");
        }
        return player.getGold() >= gold;
    }

    public boolean isEnoughGold(Long gold, Long playerId) throws NoPlayerException {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));
        return isEnoughGold(gold, player);
    }

    @Transactional
    public void takeMoney(Long gold, Player player) throws NotEnoughGoldException {
        if (isEnoughGold(gold, player)) {
            player.setGold(player.getGold() - gold);
        } else {
            throw new NotEnoughGoldException(player.getId(), gold);
        }
    }

    @Transactional
    public void takeMoney(Long gold, Long playerId) throws NotEnoughGoldException, NoPlayerException {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));

        if (isEnoughGold(gold, player)) {
            player.setGold(player.getGold() - gold);
        } else {
            throw new NotEnoughGoldException(player.getId(), gold);
        }
    }

    @Transactional
    public void returnMoney(Long gold, Player player) throws NotEnoughGoldException {
        if (isEnoughGold(gold, player)) {
            player.setGold(player.getGold() + gold);
        } else {
            throw new NotEnoughGoldException(player.getId(), gold);
        }
    }

    @Transactional
    public void returnMoney(Long gold, Long playerId) throws NotEnoughGoldException, NoPlayerException {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoPlayerException(playerId));

        if (isEnoughGold(gold, player)) {
            player.setGold(player.getGold() + gold);
        } else {
            throw new NotEnoughGoldException(player.getId(), gold);
        }
    }
}

