package game.Happy_Zombie_Farm.controller;

import game.Happy_Zombie_Farm.dto.TelegramAuthDto;
import game.Happy_Zombie_Farm.service.TelegramAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/telegram")
public class TelegramAuthController {

    private static final Logger log = LoggerFactory.getLogger(TelegramAuthController.class);

    @Autowired
    private TelegramAuthService telegramAuthService;

//    @GetMapping
//    public ResponseEntity<Resource> getAuthScript() {
//        Resource resource = new ClassPathResource("public/index.html");
//        var headers = new HttpHeaders();
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=index.html");
//        return ResponseEntity.ok().headers(headers).body(resource);
//    }

    /**
     * сюда отправляются данные, полученные после аутентификации
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public String authenticate(@RequestBody TelegramAuthDto telegramAuthDto) {
        return telegramAuthService.telegramDataIsValid(telegramAuthDto) ? "pretend-that-it-is-your-token" : "error";
    }
}