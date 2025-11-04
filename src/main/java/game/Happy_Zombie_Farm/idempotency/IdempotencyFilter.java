package game.Happy_Zombie_Farm.idempotency;

import game.Happy_Zombie_Farm.security.PlayerPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import java.io.IOException;
import java.util.Optional;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    @Autowired
    private IdempotencyService idempotencyService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        if (!"/graphql".equals(request.getRequestURI())) return true;
        return request.getHeader("Idempotency-Key") == null;
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyKey = request.getHeader("Idempotency-Key");

        String playerId = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof PlayerPrincipal p) {
            playerId = String.valueOf(p.getPlayerId());
        }

        String redisKey = "idem:" + playerId + ":" + idempotencyKey;

        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
        String body = wrappedRequest.getCachedBodyAsString();
        String requestHash = sha256(body + "|" + playerId);

        // 1. Проверяем Redis
        Optional<IdempotencyEntry> existingOpt = idempotencyService.get(redisKey);
        if (existingOpt.isPresent()) {
            IdempotencyEntry existing = existingOpt.get();

            if (!requestHash.equals(existing.getRequestHash())) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("""
                        {"error":"Idempotency key reused with different request"}""");
                return;
            }

            if (existing.getStatus() == IdempotencyStatus.COMPLETED) {
                // сразу отдаём кэшированный ответ
                response.setStatus(
                        existing.getHttpStatus() != null ? existing.getHttpStatus() : 200
                );
                if (existing.getContentType() != null) {
                    response.setContentType(existing.getContentType());
                } else {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                }
                if (existing.getResponseBody() != null) {
                    response.getWriter().write(existing.getResponseBody());
                }
                return;
            }

            if (existing.getStatus() == IdempotencyStatus.IN_PROGRESS) {
                // Простой поллинг — ждём немного, вдруг запрос успеет завершиться
                for (int i = 0; i < 10; i++) { // до ~1 сек
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                    Optional<IdempotencyEntry> afterWait = idempotencyService.get(redisKey);
                    if (afterWait.isPresent() && afterWait.get().getStatus() == IdempotencyStatus.COMPLETED) {
                        IdempotencyEntry rec = afterWait.get();
                        response.setStatus(rec.getHttpStatus() != null ? rec.getHttpStatus() : 200);
                        if (rec.getContentType() != null) {
                            response.setContentType(rec.getContentType());
                        } else {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        }
                        if (rec.getResponseBody() != null) {
                            response.getWriter().write(rec.getResponseBody());
                        }
                        return;
                    }
                }
                // не дождались — говорим, что запрос уже обрабатывается
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("""
                        {"status":"IN_PROGRESS","message":"Request is already being processed"}""");
                return;
            }
        }

        // 2. Попробуем занять ключ (IN_PROGRESS)
        boolean created = idempotencyService.putInProgress(redisKey, requestHash);
        if (!created) {
            // между get и put кто-то успел создать запись — повторяем логику
            Optional<IdempotencyEntry> again = idempotencyService.get(redisKey);
            if (again.isPresent() && again.get().getStatus() == IdempotencyStatus.COMPLETED) {
                IdempotencyEntry rec = again.get();
                response.setStatus(rec.getHttpStatus() != null ? rec.getHttpStatus() : 200);
                if (rec.getContentType() != null) {
                    response.setContentType(rec.getContentType());
                } else {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                }
                if (rec.getResponseBody() != null) {
                    response.getWriter().write(rec.getResponseBody());
                }
                return;
            }
        }

        // 3. Даём запросу отработать и сохраняем результат
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(wrappedRequest, responseWrapper);

        byte[] responseBodyBytes = responseWrapper.getContentAsByteArray();
        String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);

        idempotencyService.saveCompleted(
                redisKey,
                requestHash,
                responseBody,
                responseWrapper.getStatus(),
                responseWrapper.getContentType()
        );

        responseWrapper.copyBodyToResponse();
    }
}

