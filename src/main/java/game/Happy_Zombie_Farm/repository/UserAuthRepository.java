package game.Happy_Zombie_Farm.repository;

import game.Happy_Zombie_Farm.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    Optional<UserAuth> findByTelegramId(Long telegramId);
}

