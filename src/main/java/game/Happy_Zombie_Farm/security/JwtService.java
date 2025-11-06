package game.Happy_Zombie_Farm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * Генерим короткоживущий access-токен.
     * Кладём в клеймы player_id и username, чтобы потом можно было достать @AuthenticationPrincipal.
     */
    public String generateAccessToken(Long playerId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("playerId", playerId);
        claims.put("tokenType", "access");

        Instant now = Instant.now();
        Instant exp = now.plusMillis(jwtProperties.getAccessExpirationMs());

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(playerId))
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(exp))
                .signWith(getSignKey())
                .compact();
    }

    public String generateRefreshToken(Long playerId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("playerId", playerId);
        claims.put("tokenType", "refresh");

//        ДОБАВИТЬ ЗАПОМИНАНИЕ СТАРЫХ РЕФРЕШЕЙ!!!!!!!

        Instant now = Instant.now();
        Instant exp = now.plusMillis(jwtProperties.getRefreshExpirationMs());

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(playerId))
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(exp))
                .signWith(getSignKey())
                .compact();
    }

    public void putTokensInCookies(
        HttpServletResponse response,
        String accessToken,
        String refreshToken
    ) {
        ResponseCookie accessCookie = ResponseCookie.from(jwtProperties.getAccessCookieName(), accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtProperties.getAccessExpirationMs())
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(jwtProperties.getRefreshCookieName(), refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/auth") // refresh ходит только под /auth/*
                .maxAge(jwtProperties.getRefreshExpirationMs())
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    public void putTokensInCookies(
            HttpServletResponse response,
            String accessToken
    ) {
        ResponseCookie accessCookie = ResponseCookie.from(jwtProperties.getAccessCookieName(), accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtProperties.getAccessExpirationMs())
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractPlayerId(String token) {
        Claims claims = extractAllClaims(token);
        Object playerId = claims.get("playerId");
        if (playerId == null) {
            return Long.parseLong(claims.getSubject());
        }
        return Long.valueOf(String.valueOf(playerId));
    }

    public boolean isRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        String type = claims.get("tokenType", String.class);
        return "refresh".equals(type);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

