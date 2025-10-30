package game.Happy_Zombie_Farm.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
@ConfigurationProperties(prefix = "houses-info")
public record HousesInfoCfg(
        @NotEmpty Map<@NotNull String, @NotNull HouseCfg> type
) {
    @Validated
    public record HouseCfg(
            @PositiveOrZero int width,
            @PositiveOrZero int height,
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
