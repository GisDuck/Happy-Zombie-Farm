package game.Happy_Zombie_Farm.entity;

import game.Happy_Zombie_Farm.enums.BoardColor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private Long meat;

    @Column(nullable = false)
    private Long gold;

    @Column(nullable = false)
    private Long brain;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "board_color", columnDefinition = "board_color")
    private BoardColor boardColor;

    @OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
    private UserAuth userAuth;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<House> houses = new HashSet<>();
}
