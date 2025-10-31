package game.Happy_Zombie_Farm.service;

import game.Happy_Zombie_Farm.entity.UserAuth;
import game.Happy_Zombie_Farm.mapper.PlayerMapper;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import game.Happy_Zombie_Farm.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserAuthRepository userAuthRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerMapper playerMapper;

    public Optional<UserAuth> getOptionalUserAuthByTelegramId(Long telegramId) {
        return userAuthRepository.findByTelegramId(telegramId);
    }
}

