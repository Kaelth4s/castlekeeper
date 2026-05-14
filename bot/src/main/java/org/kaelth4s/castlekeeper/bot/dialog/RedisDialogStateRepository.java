package org.kaelth4s.castlekeeper.bot.dialog;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis-backed dialog state — enables horizontal scaling across multiple bot instances.
 * Stores steps as serialized references (same JVM), prefixes as strings.
 * Keys TTL after 30 minutes of inactivity.
 */
public class RedisDialogStateRepository implements DialogStateRepository {
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redis;
    private final Map<Long, DialogStep> stepCache = new java.util.concurrent.ConcurrentHashMap<>();

    public RedisDialogStateRepository(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void putStep(Long chatId, DialogStep step) {
        stepCache.put(chatId, step);
        redis.opsForValue().set(stepKey(chatId), "active", TTL);
    }

    @Override
    public Optional<DialogStep> getStep(Long chatId) {
        if (Boolean.FALSE.equals(redis.hasKey(stepKey(chatId)))) return Optional.empty();
        return Optional.ofNullable(stepCache.get(chatId));
    }

    @Override
    public void removeStep(Long chatId) {
        stepCache.remove(chatId);
        redis.delete(stepKey(chatId));
    }

    @Override
    public void putPrefix(Long chatId, String prefix) {
        redis.opsForValue().set(prefixKey(chatId), prefix != null ? prefix : "menu", TTL);
    }

    @Override
    public Optional<String> getPrefix(Long chatId) {
        String v = redis.opsForValue().get(prefixKey(chatId));
        return Optional.ofNullable(v);
    }

    @Override
    public void removePrefix(Long chatId) {
        redis.delete(prefixKey(chatId));
    }

    private static String stepKey(Long chatId) { return "ck:step:" + chatId; }
    private static String prefixKey(Long chatId) { return "ck:prefix:" + chatId; }
}
