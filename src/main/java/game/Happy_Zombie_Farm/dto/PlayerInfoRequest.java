package game.Happy_Zombie_Farm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerInfoRequest {
    @NotNull
    private Long telegramId;
    @NotNull
    private String photoUrl;
    @NotNull
    private String username;
}