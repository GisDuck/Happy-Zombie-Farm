package game.Happy_Zombie_Farm.repository;

import game.Happy_Zombie_Farm.model.BuildingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuildingTypeRepository extends JpaRepository<BuildingType, Long> {
    Optional<BuildingType> findByCode(String code);
    boolean existsByCode(String code);
}

