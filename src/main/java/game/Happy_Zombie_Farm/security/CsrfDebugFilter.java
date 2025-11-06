package game.Happy_Zombie_Farm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class CsrfDebugFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/graphql".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("X-XSRF-TOKEN");
        String cookieVal = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("XSRF-TOKEN".equals(c.getName())) {
                    cookieVal = c.getValue();
                }
            }
        }
        log.info("CSRF debug: {} {} header={} cookie={}",
                request.getMethod(), request.getRequestURI(),
                header, cookieVal);

        filterChain.doFilter(request, response);
    }
}
