package game.Happy_Zombie_Farm.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerInfoRequest {
    @NotNull
    @JsonAlias({"id", "telegramId", "telegram_id"})   // принимает и "id", и "telegramId"
    private Long telegramId;

    @JsonAlias({"photo_url", "photoUrl"}) // snake_case и camelCase
    private String photoUrl;

    @NotNull
    @JsonAlias({"username"})
    private String username;
}