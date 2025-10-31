package game.Happy_Zombie_Farm.controller;

import game.Happy_Zombie_Farm.dto.PlayerDto;
import game.Happy_Zombie_Farm.dto.TelegramAuthDto;
import game.Happy_Zombie_Farm.dto.outputDto.AuthPayloadDto;
import game.Happy_Zombie_Farm.entity.Player;
import game.Happy_Zombie_Farm.entity.UserAuth;
import game.Happy_Zombie_Farm.exception.TelegramDataNotValidException;
import game.Happy_Zombie_Farm.mapper.PlayerMapper;
import game.Happy_Zombie_Farm.security.JwtService;
import game.Happy_Zombie_Farm.service.AuthService;
import game.Happy_Zombie_Farm.service.PlayerService;
import game.Happy_Zombie_Farm.service.TelegramAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private TelegramAuthService telegramAuthService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PlayerMapper playerMapper;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private AuthService authService;

    private static final String REFRESH_COOKIE = "HZF_REFRESH";

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
    @PostMapping("/telegram-login")
    public ResponseEntity<AuthPayloadDto> login(@RequestBody TelegramAuthDto telegramAuthDto,
                                               HttpServletResponse response) {
        if (telegramAuthService.telegramDataIsValid(telegramAuthDto)) {
            Optional<UserAuth> optionalUserAuth = authService.getOptionalUserAuthByTelegramId(telegramAuthDto.id());

            Player player;
            if (optionalUserAuth.isEmpty()) {
                player = telegramAuthService.registerTelegramUser(telegramAuthDto);
            } else {
                player = optionalUserAuth.get().getPlayer();
            }

            String accessToken = jwtService.generateAccessToken(player.getId(), player.getUsername());
            String refreshToken = jwtService.generateRefreshToken(player.getId());

            // 3. возвращаем access в теле
            ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofDays(14))
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            AuthPayloadDto dto = new AuthPayloadDto(playerMapper.toDto(player), accessToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(dto);
        } else {
            throw new TelegramDataNotValidException(telegramAuthDto.id());
        }
    }

    /**
     * Этот эндпоинт вызывается фронтом, когда access истёк.
     * Мы смотрим refresh в cookie и если он ок — выдаём новый access.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthPayloadDto> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            // проверить подпись и что это именно refresh
            if (!jwtService.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(401).build();
            }

            Long playerId = jwtService.extractPlayerId(refreshToken);
            PlayerDto playerDto = playerService.getPlayerDto(playerId);

            String newAccessToken = jwtService.generateAccessToken(playerDto.id(), playerDto.username());

            // Обновим для рефреш время жизни
            ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofDays(14))
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            AuthPayloadDto dto = new AuthPayloadDto(playerDto, newAccessToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(dto);

        } catch (Exception e) {
            // подпись не сошлась / истёк / не refresh
            return ResponseEntity.status(401).build();
        }
    }


}