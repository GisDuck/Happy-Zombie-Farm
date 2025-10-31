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

        Date now = new Date();
        Date expiry = new Date(now.getTime() + props.getAccessExpirationMs());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(playerId.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, props.getSecret())
                .compact();
    }

    /**
     * Генерим refresh-токен для конкретного игрока.
     */
    public String generateRefreshToken(Long playerId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("player_id", playerId);
        claims.put("token_type", "refresh");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + props.getRefreshExpirationMs());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(playerId.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, props.getSecret())
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
        String type = claims.get("token_type", String.class);
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

