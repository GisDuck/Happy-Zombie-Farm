package game.Happy_Zombie_Farm.service;

import game.Happy_Zombie_Farm.model.Player;
import game.Happy_Zombie_Farm.repository.PlayerBoardRepository;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

//dflkgdflkgjdflg
@Service
@RequiredArgsConstructor
public class PlayerBoardService {
    private final PlayerRepository playerRepository;
    private final PlayerBoardRepository playerBoardRepository;
}
