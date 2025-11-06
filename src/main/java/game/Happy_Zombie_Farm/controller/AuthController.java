package game.Happy_Zombie_Farm.controller;

import game.Happy_Zombie_Farm.dto.PlayerDto;
import game.Happy_Zombie_Farm.dto.TelegramAuthDto;
import game.Happy_Zombie_Farm.entity.Player;
import game.Happy_Zombie_Farm.entity.UserAuth;
import game.Happy_Zombie_Farm.exception.TelegramDataNotValidException;
import game.Happy_Zombie_Farm.mapper.PlayerMapper;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import game.Happy_Zombie_Farm.security.JwtProperties;
import game.Happy_Zombie_Farm.security.JwtService;
import game.Happy_Zombie_Farm.service.AuthService;
import game.Happy_Zombie_Farm.service.PlayerService;
import game.Happy_Zombie_Farm.service.TelegramAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private TelegramAuthService telegramAuthService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private PlayerMapper playerMapper;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private AuthService authService;

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
    @Transactional
    public ResponseEntity<Void> login(@RequestBody TelegramAuthDto telegramAuthDto,
                                               HttpServletResponse response) {
        if (telegramAuthService.telegramDataIsValid(telegramAuthDto)) {
            Optional<UserAuth> optionalUserAuth = authService.getOptionalUserAuthByTelegramId(telegramAuthDto.id());

            Player player;
            if (optionalUserAuth.isEmpty()) {
                player = telegramAuthService.registerTelegramUser(telegramAuthDto);
            } else {
                player = optionalUserAuth.get().getPlayer();
                player.setPhotoUrl(telegramAuthDto.photoUrl());
                playerRepository.save(player);
            }

            String accessToken = jwtService.generateAccessToken(player.getId());
            String refreshToken = jwtService.generateRefreshToken(player.getId());

            jwtService.putTokensInCookies(response, accessToken, refreshToken);

            return ResponseEntity.ok().build();
        } else {
            throw new TelegramDataNotValidException(telegramAuthDto.id());
        }
    }

    /**
     * Этот эндпоинт вызывается фронтом, когда access истёк.
     * Мы смотрим refresh в cookie и если он ок — выдаём новый access.
     */
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<Void> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractCookie(request, jwtProperties.getRefreshCookieName());
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            // проверить подпись и что это именно refresh
            if (!jwtService.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(401).build();
            }

//            ДОБАВИТЬ ПРОВЕРКУ РЕФРЕША НА НАЛИЧИЕ В БАЗЕ И ЕСЛИ ОН ЕСТЬ ВЫДАВАТЬ ОШИБКУ ЧТО СТАРЫЙ ТОКЕН

            Long playerId = jwtService.extractPlayerId(refreshToken);
            PlayerDto playerDto = playerService.getPlayerDto(playerId);

            String newAccessToken = jwtService.generateAccessToken(playerDto.id());
            String newRefreshToken = jwtService.generateRefreshToken(playerDto.id());

            ResponseCookie accessCookie = ResponseCookie.from(jwtProperties.getAccessCookieName(), newAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(jwtProperties.getAccessExpirationMs())
                    .sameSite("Lax")
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from(jwtProperties.getRefreshCookieName(), newRefreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/auth") // refresh ходит только под /auth/*
                    .maxAge(jwtProperties.getRefreshExpirationMs())
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            // подпись не сошлась / истёк / не refresh
            return ResponseEntity.status(401).build();
        }

    }

    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from(jwtProperties.getAccessCookieName(), "")
                .httpOnly(true).secure(true).path("/").maxAge(0).sameSite("Lax").build();
        ResponseCookie refreshCookie = ResponseCookie.from(jwtProperties.getRefreshCookieName(), "")
                .httpOnly(true).secure(true).path("/auth").maxAge(0).sameSite("Lax").build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok().build();
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (jakarta.servlet.http.Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }


}