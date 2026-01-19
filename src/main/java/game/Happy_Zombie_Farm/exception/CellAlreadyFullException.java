package game.Happy_Zombie_Farm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CellAlreadyFullException extends RuntimeException {
    private final Long playerId;

    public CellAlreadyFullException(Long playerId, Integer cell) {
        super("This cell" + cell + " already full for player" + playerId);
        this.playerId = playerId;
    }

    public CellAlreadyFullException(String message) {
        super(message);
        this.playerId = null;
    }

    public Long getPlayerId() {
        return playerId;
    }
}
