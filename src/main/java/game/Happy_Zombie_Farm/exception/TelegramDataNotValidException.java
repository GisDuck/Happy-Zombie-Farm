package game.Happy_Zombie_Farm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TelegramDataNotValidException extends RuntimeException {
    private final Long telegramId;

    public TelegramDataNotValidException(Long telegramId) {
        super("Telegram data not valid by id=" + telegramId);
        this.telegramId = telegramId;
    }

    public TelegramDataNotValidException(String message) {
        super(message);
        this.telegramId = null;
    }

    public Long getPLayerId() {
        return telegramId;
    }
}
