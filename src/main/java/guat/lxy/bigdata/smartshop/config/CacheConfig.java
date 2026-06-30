package guat.lxy.bigdata.smartshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置：声明式缓存走 Redis（替代原 ConcurrentMapCacheManager）
 *
 * - @Cacheable/@CachePut/@CacheEvict 命中 cacheNames=category / product
 * - 通过 RedisCacheManager 把这两个命名空间绑到 Redis
 * - 编程式缓存（RedisTemplate）见 Service 层的 findAllWithCache / searchWithCache 方法
 *
 * 注意：Spring Boot 4.x 默认 ObjectMapper 是 Jackson 3（tools.jackson.*），
 *       spring-data-redis 内置的 GenericJackson2JsonRedisSerializer 基于 Jackson 2，
 *       两个版本无法直接共存。这里改用 JdkSerializationRedisSerializer（JDK 原生序列化）：
 *       - 零三方依赖
 *       - 项目所有 entity 已 implements Serializable
 *       - 缓存值在 Redis 中以二进制存储
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${smartshop.cache.key-prefix}")
    private String keyPrefix;

    /** 默认 TTL：10 分钟 */
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    /**
     * 编程式缓存使用的 RedisTemplate：key=String，value=JDK 序列化
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jdkSerializer);
        template.setHashValueSerializer(jdkSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 声明式缓存（@Cacheable 等）使用的 CacheManager
     * - 全局默认 TTL：10 分钟
     * - category 命名空间：10 分钟
     * - product  命名空间：5 分钟
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer();

        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .computePrefixWith(name -> keyPrefix + "cache:" + name + ":")
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jdkSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put("category", baseConfig.entryTtl(Duration.ofMinutes(10)));
        perCache.put("product", baseConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(baseConfig)
                .withInitialCacheConfigurations(perCache)
                .transactionAware()
                .build();
    }
}