package game.Happy_Zombie_Farm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoPlayerException extends RuntimeException {
    private final Integer telegramId;

    public NoPlayerException(Integer telegramId) {
        super("Player not found by telegramId=" + telegramId);
        this.telegramId = telegramId;
    }

    public NoPlayerException(String message) {
        super(message);
        this.telegramId = null;
    }

    public Integer getTelegramId() {
        return telegramId;
    }
}
