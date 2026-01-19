package game.Happy_Zombie_Farm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Profile("dev")
public class DevPlayerIdFilter extends OncePerRequestFilter {

    private final long defaultPlayerId;

    public DevPlayerIdFilter(@Value("${app.dev.player-id:1}") long defaultPlayerId) {
        this.defaultPlayerId = defaultPlayerId;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // Можно переопределять с фронта: X-Player-Id: 123
        String hdr = req.getHeader("PLAYER-ID");
        long playerId = (hdr != null && !hdr.isBlank()) ? Long.parseLong(hdr) : defaultPlayerId;

        var principal = new PlayerPrincipal(playerId);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, res);
    }
}
