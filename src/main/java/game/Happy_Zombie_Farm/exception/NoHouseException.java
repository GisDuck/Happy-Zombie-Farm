package game.Happy_Zombie_Farm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoHouseException extends RuntimeException {
    private final Long houseId;

    public NoHouseException(Long houseId) {
        super("House not found by id=" + houseId);
        this.houseId = houseId;
    }

    public NoHouseException(String message) {
        super(message);
        this.houseId = null;
    }

    public Long getHouseId() {
        return houseId;
    }
}
