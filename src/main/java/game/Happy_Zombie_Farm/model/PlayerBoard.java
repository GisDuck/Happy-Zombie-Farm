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
@Table(name = "player_boards")
public class PlayerBoard {

    @Id
    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(
        name = "player_id",
        referencedColumnName = "telegram_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_player_boards_player")
    )
    @NotNull
    private Player player;

    // 1024 бит (32x32): 1 — занято, 0 — свободно
    @Column(nullable = false, columnDefinition = "bit(1024)")
    @NotBlank
    @Pattern(
        regexp = "[01]{1024}",
        message = "occupancy должен быть строкой из 1024 символов 0/1"
    )
    private String occupancy;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
