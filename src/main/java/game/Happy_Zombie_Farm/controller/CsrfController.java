package game.Happy_Zombie_Farm.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class CsrfController {

    @GetMapping("/gen-csrf")
    public ResponseEntity<Void> genCsrf(HttpServletResponse response) {

        String token = UUID.randomUUID().toString();

        ResponseCookie csrfCookie = ResponseCookie.from("XSRF-TOKEN", token)
                .httpOnly(false)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofHours(12))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, csrfCookie.toString());
        return ResponseEntity.noContent().build();
    }
}
