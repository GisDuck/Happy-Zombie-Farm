package game.Happy_Zombie_Farm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "player_buildings",
    indexes = {
        @Index(name = "ix_player_buildings_player", columnList = "player_id"),
        @Index(name = "ix_player_buildings_player_xy", columnList = "player_id,origin_x,origin_y")
    }
)
public class PlayerBuilding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @NotNull
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "player_id",
        referencedColumnName = "telegram_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_player_buildings_player")
    )
    @NotNull
    private Player player;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "building_type_id",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_player_buildings_type")
    )
    @NotNull
    private BuildingType buildingType;

    // доска 32x32 → координаты 0..31
    @Column(name = "origin_x", nullable = false)
    @NotNull
    @Min(0) @Max(31)
    private Short originX;

    @Column(name = "origin_y", nullable = false)
    @NotNull
    @Min(0) @Max(31)
    private Short originY;

    @Column(name = "placed_at", nullable = false)
    @NotNull
    private OffsetDateTime placedAt;

    @PrePersist
    public void prePersist() {
        if (placedAt == null) placedAt = OffsetDateTime.now();
    }
}
