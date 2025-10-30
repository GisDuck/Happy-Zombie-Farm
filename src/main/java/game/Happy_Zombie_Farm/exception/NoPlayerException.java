package game.Happy_Zombie_Farm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoPlayerException extends RuntimeException {
    private final Long playerId;

    public NoPlayerException(Long playerId) {
        super("Player not found by id=" + playerId);
        this.playerId = playerId;
    }

    public NoPlayerException(String message) {
        super(message);
        this.playerId = null;
    }

    public Long getPLayerId() {
        return playerId;
    }
}
