package game.Happy_Zombie_Farm.config;

import game.Happy_Zombie_Farm.idempotency.IdempotencyFilter;
import game.Happy_Zombie_Farm.security.CsrfDebugFilter;
import game.Happy_Zombie_Farm.security.JwtAuthFilter;
import game.Happy_Zombie_Farm.security.SpaCsrfTokenRequestHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    @Autowired
    private IdempotencyFilter idempotencyFilter;
    @Autowired
    private CsrfDebugFilter csrfDebugFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/auth/**")   // логин/refresh/logout можно без CSRF
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // REST логин можно
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/", "/index", "/index.html").permitAll()
                    .requestMatchers("/login", "/login.html").permitAll()
                    // всё остальное — только с токеном
                    .anyRequest().authenticated()
            )
            .addFilterBefore(csrfDebugFilter, CsrfFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(idempotencyFilter, JwtAuthFilter.class)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        if (isHtmlRequest(request)) {
                            // Нету доступа к HTML → редирект на главную
                            String uri = URLEncoder.encode(request.getRequestURI(), StandardCharsets.UTF_8);
                            response.sendRedirect("/?redirect=" + uri);
                        } else {
                            // fetch / graphql / просто 401
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        }
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        if (isHtmlRequest(request)) {
                            response.sendRedirect("/");
                        } else {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        }
                    })
            );

        return http.build();
    }

    private boolean isHtmlRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        return (accept != null && accept.contains("text/html"))
                || uri.endsWith(".html");
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/static/**",
                "/public/**", "/css/**", "/js/**", "/img/**", "/favicon.ico", "/error"
        );
    }

    // вдруг где-то понадобится
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
