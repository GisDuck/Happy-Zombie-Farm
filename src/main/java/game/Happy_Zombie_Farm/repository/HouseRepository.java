package game.Happy_Zombie_Farm.repository;

import game.Happy_Zombie_Farm.entity.House;
import game.Happy_Zombie_Farm.enums.HouseType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HouseRepository extends JpaRepository<House, Long> {

    List<House> findByPlayerId(Long playerId);

    List<House> findByPlayerIdAndType(Long playerId, HouseType type);
}
