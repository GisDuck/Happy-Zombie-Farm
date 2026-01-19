package game.Happy_Zombie_Farm.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private JwtProperties jwtProperties;


    private String resolveAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (
                    jwtProperties
                    .getAccessCookieName()
                    .equals(cookie.getName())
                ) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (jwtProperties.getRefreshCookieName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isHtmlRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        return (accept != null && accept.contains("text/html"))
                || uri.endsWith(".html");
    }

    private void authenticateFromAccessToken(String token, HttpServletRequest request) {
        Claims claims = jwtService.extractAllClaims(token);
        Long playerId = Long.valueOf(String.valueOf(claims.get("playerId")));

        PlayerPrincipal principal = new PlayerPrincipal(playerId);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("JwtAuthFilter: authenticated playerId={} path={} header={} cookies={}",
                playerId, request.getRequestURI(),
                request.getHeader("X-XSRF-TOKEN"),
                request.getCookies());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String accessToken = resolveAccessToken(request);

        if (accessToken != null
            && jwtService.isTokenValid(accessToken)
            && SecurityContextHolder.getContext().getAuthentication() == null) {

            authenticateFromAccessToken(accessToken, request);
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null
                && isHtmlRequest(request)) {

            String refreshToken = resolveRefreshToken(request);

            log.info("JwtAuthFilter: try get html with refreshToken={} path={}",
                    refreshToken, request.getRequestURI());

            if (refreshToken != null
                    && jwtService.isTokenValid(refreshToken)
                    && jwtService.isRefreshToken(refreshToken)) {

                Long playerId = jwtService.extractPlayerId(refreshToken);

                String newAccessToken = jwtService.generateAccessToken(playerId);
                String newRefreshToken = jwtService.generateRefreshToken(playerId);

                jwtService.putTokensInCookies(response, newAccessToken, newRefreshToken);

                authenticateFromAccessToken(newAccessToken, request);

                log.info("JwtAuthFilter: get new tokens and authorized for playerId={} path={}",
                        playerId, request.getRequestURI());

                filterChain.doFilter(request, response);
                return;
            }
        }

        // 3. Для API/GraphQL или если refresh невалиден — просто идём дальше.
        // Security/EntryPoint сам вернёт 401 или редирект на /login.
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/")
                || path.equals("/")
                || path.equals("/index")
                || path.equals("/index.html")
                || path.startsWith("/public/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/img/")
                || path.equals("/favicon.ico")
                || path.equals("/error");
        }
}
