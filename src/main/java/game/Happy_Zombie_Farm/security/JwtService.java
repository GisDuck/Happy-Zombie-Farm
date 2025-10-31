package game.Happy_Zombie_Farm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Autowired
    private JwtProperties props;

    /**
     * Генерим короткоживущий access-токен.
     * Кладём в клеймы player_id и username, чтобы потом можно было достать @AuthenticationPrincipal.
     */
    public String generateAccessToken(Long playerId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("playerId", playerId);
        claims.put("username", username);
        claims.put("tokenType", "access");

        Instant now = Instant.now();
        Instant exp = now.plusMillis(props.getAccessExpirationMs());

        return Jwts.builder()
                .claims(claims)                // вместо setClaims(...)
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

        Instant now = Instant.now();
        Instant exp = now.plusMillis(props.getRefreshExpirationMs());

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(playerId))
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(exp))
                .signWith(getSignKey())
                .compact();
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

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("username", String.class);
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
        byte[] keyBytes = Decoders.BASE64.decode(props.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

