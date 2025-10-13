package game.Happy_Zombie_Farm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "building_types",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_building_types_code", columnNames = {"code"})
    }
)
public class BuildingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @NotNull
    private Long id;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 64)
    private String code;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(nullable = false)
    @NotNull
    @Min(1) @Max(32)
    private Short width;

    @Column(nullable = false)
    @NotNull
    @Min(1) @Max(32)
    private Short height;

    @Column(name = "money_delta_per_minute", nullable = false)
    @NotNull
    private Integer moneyDeltaPerMinute = 0;

    @Column(name = "brains_delta_per_minute", nullable = false)
    @NotNull
    private Integer brainsDeltaPerMinute = 0;

    @Column(name = "build_cost_money", nullable = false)
    @NotNull @PositiveOrZero
    private Long buildCostMoney = 0L;

    @Column(name = "build_cost_brains", nullable = false)
    @NotNull @PositiveOrZero
    private Long buildCostBrains = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "buildingType", cascade = CascadeType.ALL)
    private List<PlayerBuilding> instances = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
