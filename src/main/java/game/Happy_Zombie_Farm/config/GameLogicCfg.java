package game.Happy_Zombie_Farm.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
@ConfigurationProperties(prefix = "game-logic")
public record GameLogicCfg(
        @NotEmpty Production production,
        @NotEmpty Conversion conversion,
        @NotNull Double returnGoldForHouse
) {
    @Validated
    public record Production(
            @Positive int meatPerCowPerSec
    ) {}

    @Validated
    public record Conversion(
            @Positive int meatPerOneBrain,
            @Positive int brainPerOneCoin
    ) {}
}
