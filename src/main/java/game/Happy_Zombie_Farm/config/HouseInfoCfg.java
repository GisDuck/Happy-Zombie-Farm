package game.Happy_Zombie_Farm.config;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "house-info")
public record HouseInfoCfg(
        @NotNull BuildingCfg farm,
        @NotNull BuildingCfg house,
        @NotNull DecorCfg   decor
) {
    //Общая схема для FARM/HOUSE
    @Validated
    public record BuildingCfg(
            @Positive int width,
            @Positive int height,
            @NotNull Map<@Positive Integer, LevelCfg> levels,
            @NotEmpty List<SkinCfg> skins
    ) {}

    @Validated
    public record DecorCfg(
            @NotEmpty List<SkinCfg> skins
    ) {}

    public record LevelCfg(
            @PositiveOrZero int price,
            @PositiveOrZero Integer cows   // для HOUSE можно игнорировать, если не нужно
    ) {}

    // Скин. Для DECOR в скинах width/height обязательны, для FARM/HOUSE нет
    @Validated
    public record SkinCfg(
            @Positive int id,
            @PositiveOrZero int price,
            @PositiveOrZero Integer width,
            @PositiveOrZero Integer height
    ) {}
}
