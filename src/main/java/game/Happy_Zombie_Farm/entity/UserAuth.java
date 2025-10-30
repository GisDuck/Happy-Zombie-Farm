package game.Happy_Zombie_Farm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users_auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuth {

    @Id
    @Column(name = "telegram_id", nullable = false)
    private Long telegramId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inner_id", nullable = false)
    private Player player;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
