package guat.lxy.bigdata.smartshop.service.impl;

import guat.lxy.bigdata.smartshop.entity.Category;
import guat.lxy.bigdata.smartshop.mapper.CategoryMapper;
import guat.lxy.bigdata.smartshop.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * CategoryServiceImpl
 *
 * 缓存策略（双重缓存）：
 * 1. 声明式（@Cacheable / @CacheEvict）：针对高频的单个查询与变更，
 *    由 RedisCacheManager 统一落到 Redis，key 前缀 smartshop:cache:category:*
 * 2. 编程式（RedisTemplate）：针对分类列表（findAll），单独走 Redis 键
 *    smartshop:list:category:all，TTL = 10 分钟，写操作同步清理，
 *    读取时打命中日志用于证明缓存生效。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private static final String LIST_KEY = "smartshop:list:category:all";

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${smartshop.cache.ttl.category-list}")
    private long listTtlSeconds;

    // ===================== 声明式缓存 =====================

    @Override
    @Cacheable(value = "category", key = "'all'")
    public List<Category> findAll() {
        log.info("[Category#findAll] Cache MISS - 查询 MySQL -> category");
        return categoryMapper.findAll();
    }

    @Override
    @Cacheable(value = "category", key = "#id")
    public Category findById(Integer id) {
        log.info("[Category#findById({})] Cache MISS - 查询 MySQL", id);
        return categoryMapper.findById(id);
    }

    // ===================== 编程式 Redis 缓存 =====================

    /**
     * 编程式缓存版的分类列表：
     * 先查 Redis -> 命中则直接返回（无 SQL） -> 未命中查 MySQL -> 回写 Redis（带 TTL）
     * 用于 Welcome 页 / 侧边栏下拉等需要"分类全量列表"的场景。
     */
    @SuppressWarnings("unchecked")
    public List<Category> findAllWithCache() {
        // 1) 查 Redis
        Object cached = redisTemplate.opsForValue().get(LIST_KEY);
        if (cached instanceof List) {
            log.info("[Category#findAllWithCache] ✅ Redis 缓存命中，不执行 SQL");
            return (List<Category>) cached;
        }
        // 2) 未命中，查 MySQL
        log.info("[Category#findAllWithCache] ❌ Redis 缓存未命中，执行 SQL");
        List<Category> list = categoryMapper.findAll();
        // 3) 写回 Redis，带过期时间
        if (list != null) {
            redisTemplate.opsForValue().set(LIST_KEY, list, listTtlSeconds, TimeUnit.SECONDS);
            log.info("[Category#findAllWithCache] 已写入 Redis, key={}, ttl={}s, size={}",
                    LIST_KEY, listTtlSeconds, list.size());
        }
        return list;
    }

    // ===================== 写操作 =====================

    @Override
    @CacheEvict(value = "category", key = "'all'")
    public boolean save(Category category) {
        int rows = categoryMapper.insert(category);
        // 编程式缓存清理
        Boolean deleted = redisTemplate.delete(LIST_KEY);
        log.info("[Category#save] 写 MySQL rows={}, 已清理 Redis 列表缓存 key={}, deleted={}",
                rows, LIST_KEY, deleted);
        return rows > 0;
    }

    @Override
    @CacheEvict(value = "category", key = "#category.id")
    public boolean update(Category category) {
        int rows = categoryMapper.update(category);
        Boolean deleted = redisTemplate.delete(LIST_KEY);
        log.info("[Category#update id={}] 写 MySQL rows={}, 已清理 Redis 列表缓存 deleted={}",
                category.getId(), rows, deleted);
        return rows > 0;
    }

    @Override
    @CacheEvict(value = "category", key = "#id")
    public boolean deleteById(Integer id) {
        int rows = categoryMapper.deleteById(id);
        Boolean deleted = redisTemplate.delete(LIST_KEY);
        log.info("[Category#deleteById id={}] 写 MySQL rows={}, 已清理 Redis 列表缓存 deleted={}",
                id, rows, deleted);
        return rows > 0;
    }
}