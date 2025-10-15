package game.Happy_Zombie_Farm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "players",
    indexes = {
        @Index(name = "ix_players_last_sync_at", columnList = "last_sync_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_players_username", columnNames = {"username"})
    }
)
public class Player {

    @Id
    @Column(name = "telegram_id", nullable = false)
    @NotNull
    private Integer telegramId;

    @Column
    private String username;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(nullable = false)
    @NotNull
    @PositiveOrZero
    private Long brains = 0L;

    @Column(nullable = false)
    @NotNull
    @PositiveOrZero
    private Long money = 0L;

    @Column(name = "brains_per_minute", nullable = false)
    @NotNull
    private Integer brainsPerMinute = 0;

    @Column(name = "money_per_minute", nullable = false)
    @NotNull
    private Integer moneyPerMinute = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private OffsetDateTime updatedAt;

    // Relations
    @OneToMany(
        mappedBy = "player",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<PlayerBuilding> buildings = new ArrayList<>();

    @OneToOne(
        mappedBy = "player",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private PlayerBoard board;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
