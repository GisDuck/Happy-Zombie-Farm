package game.Happy_Zombie_Farm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class GraphQlLoggingConfig {

    @Bean
    public WebGraphQlInterceptor graphQlLoggingInterceptor() {
        return (request, chain) -> {
            long start = System.currentTimeMillis();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Object principal = (auth != null ? auth.getPrincipal() : null);

            String idemKey = request.getHeaders().getFirst("Idempotency-Key");
            String opName  = request.getOperationName();
            String query   = request.getDocument();
            if (query != null && query.length() > 300) {
                query = query.substring(0, 300) + "...";
            }

            log.info("GQL request opName={} idemKey={} principal={} vars={} query={}",
                    opName, idemKey, principal, request.getVariables(), query);

            return chain.next(request).doOnNext(response -> {
                long took = System.currentTimeMillis() - start;
                log.info("GQL response opName={} took={}ms errors={}",
                        opName, took, response.getErrors());
            });
        };
    }
}
