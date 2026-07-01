package guat.lxy.bigdata.smartshop.util;

import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 统一 Redis 缓存工具：所有缓存操作走这里，日志一致、格式统一。
 *
 * 日志格式：
 *   ✅ [CACHE HIT]  key=xxx
 *   🔍 [CACHE MISS] key=xxx, loading from DB...
 *   🗑️ [CACHE EVICT] pattern=xxx, deleted=N keys
 */
@Component
public class CacheHelper {

    private static final Logger log = LoggerFactory.getLogger(CacheHelper.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheHelper(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 通用缓存读取：命中返回缓存，未命中执行 loader 并写入 Redis。
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, long ttlSeconds, Supplier<T> loader) {
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("✅ [CACHE HIT]  key={}", key);
            return (T) cached;
        }
        log.info("🔍 [CACHE MISS] key={}, loading from DB...", key);
        T result = loader.get();
        if (result != null) {
            redisTemplate.opsForValue().set(key, result, ttlSeconds, TimeUnit.SECONDS);
        }
        return result;
    }

    /**
     * 分页缓存读取：用 SerializablePage 包装 PageInfo（PageInfo 本身不可序列化）。
     */
    @SuppressWarnings("unchecked")
    public <T> PageInfo<T> getOrLoadPage(String key, long ttlSeconds, Supplier<PageInfo<T>> loader) {
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof SerializablePage) {
            log.info("✅ [CACHE HIT]  key={}", key);
            return ((SerializablePage<T>) cached).toPageInfo();
        }
        log.info("🔍 [CACHE MISS] key={}, loading from DB...", key);
        PageInfo<T> result = loader.get();
        redisTemplate.opsForValue().set(key, SerializablePage.of(result), ttlSeconds, TimeUnit.SECONDS);
        return result;
    }

    /**
     * 删除匹配 pattern 的所有缓存键。
     */
    public void evictByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            Long count = redisTemplate.delete(keys);
            log.info("🗑️ [CACHE EVICT] pattern={}, deleted={} keys", pattern, count);
        }
    }

    /**
     * 删除单个缓存键。
     */
    public void evict(String key) {
        Boolean ok = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(ok)) {
            log.info("🗑️ [CACHE EVICT] key={}", key);
        }
    }
}