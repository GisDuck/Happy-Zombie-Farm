package game.Happy_Zombie_Farm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class CsrfDebugFilter extends OncePerRequestFilter {

    private final CsrfTokenRepository repo;

    public CsrfDebugFilter(CsrfTokenRepository repo) {
        this.repo = repo;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/graphql".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        CsrfToken token = repo.loadToken(request);
        String expected = token != null ? token.getToken() : null;
        String headerName = token != null ? token.getHeaderName() : "X-XSRF-TOKEN";
        String headerVal = request.getHeader(headerName);

        String cookieVal = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("XSRF-TOKEN".equals(c.getName())) {
                    cookieVal = c.getValue();
                }
            }
        }

        log.info("CSRF debug2: expected={}, headerName={}, headerVal={}, cookie={}",
                expected, headerName, headerVal, cookieVal);

        filterChain.doFilter(request, response);
    }
}
