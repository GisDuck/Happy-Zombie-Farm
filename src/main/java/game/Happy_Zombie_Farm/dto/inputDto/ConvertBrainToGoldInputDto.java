package game.Happy_Zombie_Farm.dto.inputDto;

public record ConvertBrainToGoldInputDto(
        Long brainToSpend,
        Long expectedGold
) {}
