package game.Happy_Zombie_Farm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotEnoughGoldException extends RuntimeException {
    private final Long playerId;
    private final Long gold;

    public NotEnoughGoldException(Long playerId, Long gold) {
        super("Player not enough gold id = " + playerId + "gold = " + gold);
        this.playerId = playerId;
        this.gold = gold;
    }

    public NotEnoughGoldException(String message) {
        super(message);
        this.playerId = null;
        this.gold = null;
    }

    public Long getPLayerId() {
        return playerId;
    }

    public Long getGold() {
        return gold;
    }
}
