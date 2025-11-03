package game.Happy_Zombie_Farm.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class WrongSkinHouseParamException extends RuntimeException {
    private final String skin;

    public WrongSkinHouseParamException(String type, String skin) {
        super("Wrong skin house param skin=" + skin + "for type=" + type);
        this.skin = skin;
    }
}
