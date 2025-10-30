package game.Happy_Zombie_Farm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotThisPlayerHouseIdException extends RuntimeException {
    private final Long houseId;

    public NotThisPlayerHouseIdException(Long houseId) {
        super("This player doesn't have house with id = " + houseId);
        this.houseId = houseId;
    }

    public NotThisPlayerHouseIdException(String message) {
        super(message);
        this.houseId = null;
    }

    public Long getHouseId() {
        return houseId;
    }
}
