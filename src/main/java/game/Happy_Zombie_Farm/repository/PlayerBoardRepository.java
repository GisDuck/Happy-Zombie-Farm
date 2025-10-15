package game.Happy_Zombie_Farm.repository;

import game.Happy_Zombie_Farm.model.PlayerBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlayerBoardRepository extends JpaRepository<PlayerBoard, Integer> {
    Optional<PlayerBoard> findByPlayerId(Integer playerId);
    boolean existsByPlayerId(Integer playerId);
}

