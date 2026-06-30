package guat.lxy.bigdata.smartshop.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
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
 * - 编程式缓存（RedisTemplate）见 Service 层的 findAllWithCache / searchWithPage 方法
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${smartshop.cache.key-prefix}")
    private String keyPrefix;

    /** 默认 TTL：10 分钟 */
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    /** 共享 ObjectMapper：处理 JSR-310 时间类型 + 开启默认类型以支持 List/嵌套对象反序列化 */
    private static final ObjectMapper REDIS_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
            .activateDefaultTyping(
                    BasicPolymorphicTypeValidator.builder()
                            .allowIfSubType("guat.lxy.bigdata.smartshop.")
                            .allowIfSubType("java.util.")
                            .allowIfSubType("java.lang.")
                            .allowIfSubType("java.time.")
                            .build(),
                    ObjectMapper.DefaultTyping.NON_FINAL
            );

    /** 编程式缓存使用的 RedisTemplate：key=String，value=JSON */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer str = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer json = new GenericJackson2JsonRedisSerializer(REDIS_MAPPER);

        template.setKeySerializer(str);
        template.setHashKeySerializer(str);
        template.setValueSerializer(json);
        template.setHashValueSerializer(json);
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
        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .computePrefixWith(name -> keyPrefix + "cache:" + name + ":")
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(REDIS_MAPPER)))
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