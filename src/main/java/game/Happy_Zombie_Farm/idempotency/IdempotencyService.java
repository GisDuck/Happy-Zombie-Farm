package game.Happy_Zombie_Farm.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import game.Happy_Zombie_Farm.exception.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration TTL;


    public IdempotencyService(
        StringRedisTemplate redisTemplate,
        ObjectMapper objectMapper,
        @Value("${REDIS_TTL_MIN:10}") int redisTilMin
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        TTL = Duration.ofMinutes(redisTilMin);
    }

    public Optional<IdempotencyEntry> get(String redisKey) {
        String json = redisTemplate.opsForValue().get(redisKey);
        if (json == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(json, IdempotencyEntry.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean putInProgress(String redisKey, String requestHash) {
        try {
            log.info("Idempotency.putInProgress redisKey={} requestHash={}", redisKey, requestHash);

            IdempotencyEntry entry = new IdempotencyEntry();
            entry.setStatus(IdempotencyStatus.IN_PROGRESS);
            entry.setRequestHash(requestHash);
            String json = objectMapper.writeValueAsString(entry);
            // setIfAbsent = true если никто ещё не создал запись
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(redisKey, json, TTL);
            log.info("Idempotency.putInProgress result={}", ok);
            return Boolean.TRUE.equals(ok);
        } catch (Exception e) {
            log.error("Redis error in putInProgress", e);
            throw new RedisException(e.getClass() + ": " + e.getMessage());
        }
    }

    public boolean saveCompleted(String redisKey,
                              String requestHash,
                              String body,
                              int httpStatus,
                              String contentType) {
        try {
            IdempotencyEntry entry = new IdempotencyEntry();
            entry.setStatus(IdempotencyStatus.COMPLETED);
            entry.setRequestHash(requestHash);
            entry.setResponseBody(body);
            entry.setHttpStatus(httpStatus);
            entry.setContentType(contentType);
            String json = objectMapper.writeValueAsString(entry);
            redisTemplate.opsForValue().set(redisKey, json, TTL);
            return true;
        } catch (Exception e) {
            throw new RedisException(e.getClass() + ": " + e.getMessage());
        }
    }
}
