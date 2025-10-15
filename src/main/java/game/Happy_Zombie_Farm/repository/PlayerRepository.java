package game.Happy_Zombie_Farm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import game.Happy_Zombie_Farm.model.Player;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByTelegramId(Long telegramId);
    Optional<Player> findByUsername(String username);
    boolean existsByTelegramId(Integer telegramId);
    boolean existsByUsername(String username);
}
