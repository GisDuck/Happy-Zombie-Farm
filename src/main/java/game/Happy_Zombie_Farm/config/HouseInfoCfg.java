package game.Happy_Zombie_Farm.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "house-info")
public record HouseInfoCfg(
        @NotEmpty Map<@NotNull String, @NotNull HouseCfg> type
) {
    @Validated
    public record HouseCfg(
            @Positive int width,
            @Positive int height,
            @NotEmpty Map<@Positive Integer, @NotNull LevelCfg> levels,
            @NotEmpty Map<@Positive Integer, @NotNull SkinCfg> skins
    ) {}

    @Validated
    public record LevelCfg(
            @PositiveOrZero Long price,
            @PositiveOrZero Long cows,
            @PositiveOrZero Long maxBrain,
            @PositiveOrZero Long maxMeat
    ) {}

    // Скин для FARM/STORAGE
    @Validated
    public record SkinCfg(
            @PositiveOrZero Long price,
            @Positive int width,
            @Positive int height
    ) {}

}
