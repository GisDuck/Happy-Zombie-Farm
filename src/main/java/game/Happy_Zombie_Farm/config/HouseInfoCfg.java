package game.Happy_Zombie_Farm.config;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "house-info")
public record HouseInfoCfg(
        @NotNull FarmCfg  farm,
        @NotNull HouseCfg house,
        @NotNull DecorCfg decor
) {
    // ----- FARM -----
    @Validated
    public record FarmCfg(
            @Positive int width,
            @Positive int height,
            @NotEmpty Map<@Positive Integer, @NotNull FarmLevelCfg> levels,
            @NotNull List<@NotNull SkinCfg> skins
    ) {}

    @Validated
    public record FarmLevelCfg(
            @PositiveOrZero int price,
            @PositiveOrZero int cows
    ) {}

    // ----- HOUSE -----
    @Validated
    public record HouseCfg(
            @Positive int width,
            @Positive int height,
            @NotEmpty Map<@Positive Integer, @NotNull HouseLevelCfg> levels,
            @NotNull List<@NotNull SkinCfg> skins
    ) {}

    @Validated
    public record HouseLevelCfg(
            @PositiveOrZero int price,
            // YAML: max-brain / max-meat → поля ниже
            @PositiveOrZero int maxBrain,
            @PositiveOrZero int maxMeat
    ) {}

    // ----- DECOR -----
    @Validated
    public record DecorCfg(
            @NotNull List<@NotNull DecorSkinCfg> skins
    ) {}

    // Скин для FARM/HOUSE
    @Validated
    public record SkinCfg(
            @Positive int id,
            @PositiveOrZero int price
    ) {}

    // Скин для DECOR
    @Validated
    public record DecorSkinCfg(
            @Positive int id,
            @PositiveOrZero int price,
            @Positive int width,
            @Positive int height
    ) {}
}
