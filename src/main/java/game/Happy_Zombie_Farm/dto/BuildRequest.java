package game.Happy_Zombie_Farm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuildRequest {
    @NotNull
    private Long telegramId;
    @NotBlank
    private String buildingCode; // код типа строения
    @Min(0) @Max(31)
    private short originX;
    @Min(0) @Max(31)
    private short originY;
}
