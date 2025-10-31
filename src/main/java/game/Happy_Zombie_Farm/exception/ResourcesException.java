package game.Happy_Zombie_Farm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourcesException extends RuntimeException {

    public ResourcesException(String message) {
        super(message);
    }
}
