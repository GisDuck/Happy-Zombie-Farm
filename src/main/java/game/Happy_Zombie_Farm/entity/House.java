package game.Happy_Zombie_Farm.entity;

import game.Happy_Zombie_Farm.enums.HouseType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "houses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HouseType type;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "location_x", nullable = false)
    private Integer locationX;

    @Column(name = "location_y", nullable = false)
    private Integer locationY;

    @Column(nullable = false)
    private String skin;
}
