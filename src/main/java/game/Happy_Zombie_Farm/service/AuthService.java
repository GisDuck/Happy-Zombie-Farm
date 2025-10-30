package game.Happy_Zombie_Farm.service;

import game.Happy_Zombie_Farm.dto.outputDto.AuthPayloadDto;
import game.Happy_Zombie_Farm.entity.Player;
import game.Happy_Zombie_Farm.entity.UserAuth;
import game.Happy_Zombie_Farm.mapper.PlayerMapper;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import game.Happy_Zombie_Farm.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {
    @Autowired
    private UserAuthRepository userAuthRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerMapper playerMapper;


}

