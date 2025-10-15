package game.Happy_Zombie_Farm.repository;

import game.Happy_Zombie_Farm.model.PlayerBuilding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerBuildingRepository extends JpaRepository<PlayerBuilding, Long> {
}

