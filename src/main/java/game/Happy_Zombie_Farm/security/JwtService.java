package game.Happy_Zombie_Farm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
    }

    public String generateToken(Long playerId, String telegramId, String username) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(props.getExpirationMs());

        return Jwts.builder()
                .subject(String.valueOf(playerId))              // sub = playerId
                .claims(Map.of(
                        "playerId", playerId,
                        "telegramId", telegramId,
                        "username", username
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
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

