package game.Happy_Zombie_Farm.config;

import game.Happy_Zombie_Farm.security.DevPlayerIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    SecurityFilterChain devChain(HttpSecurity http, DevPlayerIdFilter devPlayerIdFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .addFilterBefore(devPlayerIdFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
