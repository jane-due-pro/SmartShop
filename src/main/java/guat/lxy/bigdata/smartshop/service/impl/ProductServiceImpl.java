package guat.lxy.bigdata.smartshop.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import guat.lxy.bigdata.smartshop.config.SerializablePage;
import guat.lxy.bigdata.smartshop.entity.Product;
import guat.lxy.bigdata.smartshop.mapper.ProductMapper;
import guat.lxy.bigdata.smartshop.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ProductServiceImpl
 *
 * 缓存策略（双重缓存）：
 * 1. 声明式（@Cacheable / @CacheEvict）：单商品查询走 Redis，key = smartshop:cache:product:{id}
 * 2. 编程式（RedisTemplate）：商品列表 + 复杂分页条件查询走 Redis
 *    - 商品全量列表 key：smartshop:list:product:all
 *    - 分页查询 key：smartshop:list:product:search:{条件哈希}，TTL 5 分钟
 *    - 任何写操作清掉 list:product:* 的所有相关 key（保证强一致）
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private static final String LIST_KEY_ALL = "smartshop:list:product:all";
    private static final String LIST_KEY_SEARCH_PREFIX = "smartshop:list:product:search:";

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${smartshop.cache.ttl.product-list}")
    private long listTtlSeconds;

    // ===================== 声明式缓存（单商品） =====================

    @Override
    @Cacheable(value = "product", key = "'all'")
    public List<Product> findAll() {
        log.info("[Product#findAll] Cache MISS - 查询 MySQL -> product (with category)");
        return productMapper.findAllWithCategory();
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public Product findById(Integer id) {
        log.info("[Product#findById({})] Cache MISS - 查询 MySQL", id);
        return productMapper.findByIdWithCategory(id);
    }

    // ===================== 编程式 Redis 缓存（列表） =====================

    /**
     * Welcome 页用的"全量商品列表"，对应 key：smartshop:list:product:all
     */
    @SuppressWarnings("unchecked")
    public List<Product> findAllWithCache() {
        Object cached = redisTemplate.opsForValue().get(LIST_KEY_ALL);
        if (cached instanceof List) {
            log.info("[Product#findAllWithCache] ✅ Redis 缓存命中，不执行 SQL");
            return (List<Product>) cached;
        }
        log.info("[Product#findAllWithCache] ❌ Redis 缓存未命中，执行 SQL");
        List<Product> list = productMapper.findAllWithCategory();
        if (list != null) {
            redisTemplate.opsForValue().set(LIST_KEY_ALL, list, listTtlSeconds, TimeUnit.SECONDS);
            log.info("[Product#findAllWithCache] 已写入 Redis, key={}, ttl={}s, size={}",
                    LIST_KEY_ALL, listTtlSeconds, list.size());
        }
        return list;
    }

    /**
     * 复杂分页条件查询的 Redis 缓存版本：
     * - 先把入参拼成稳定的 cacheKey
     * - 命中 Redis 直接返回（内部用 SerializablePage 避免 PageInfo 不可序列化的坑）
     */
    @Override
    public PageInfo<Product> searchWithPage(Integer catId, String name,
                                            Double minPrice, Double maxPrice,
                                            Integer pageNum, Integer pageSize) {
        String cacheKey = buildSearchKey(catId, name, minPrice, maxPrice, pageNum, pageSize);

        // 1) 查 Redis
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof SerializablePage) {
            log.info("[Product#searchWithPage] ✅ Redis 缓存命中, key={}, 不执行 SQL", cacheKey);
            return ((SerializablePage<Product>) cached).toPageInfo();
        }

        // 2) 查 MySQL
        log.info("[Product#searchWithPage] ❌ Redis 缓存未命中, key={}, 执行 SQL", cacheKey);
        PageHelper.startPage(pageNum, pageSize);
        List<Product> list = productMapper.searchWithCondition(catId, name, minPrice, maxPrice);
        PageInfo<Product> pageInfo = new PageInfo<>(list);

        // 3) 写回 Redis（用 SerializablePage 包装，避开 PageInfo 未实现 Serializable 的问题）
        redisTemplate.opsForValue().set(cacheKey, SerializablePage.of(pageInfo),
                listTtlSeconds, TimeUnit.SECONDS);
        log.info("[Product#searchWithPage] 已写入 Redis, key={}, ttl={}s, total={}",
                cacheKey, listTtlSeconds, pageInfo.getTotal());
        return pageInfo;
    }

    private String buildSearchKey(Integer catId, String name, Double minPrice,
                                  Double maxPrice, Integer pageNum, Integer pageSize) {
        // 用 | 分隔的稳定 key，参数顺序固定
        String safeName = name == null ? "" : name.replace('|', '_');
        return LIST_KEY_SEARCH_PREFIX
                + "c=" + (catId == null ? "a" : catId)
                + "|n=" + safeName
                + "|min=" + (minPrice == null ? "0" : minPrice)
                + "|max=" + (maxPrice == null ? "inf" : maxPrice)
                + "|p=" + pageNum
                + "|s=" + pageSize;
    }

    // ===================== 写操作（保持数据强一致） =====================

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product", key = "'all'"),
            @CacheEvict(value = "product", key = "#product.id")
    })
    public boolean save(Product product) {
        int rows = productMapper.insert(product);
        evictAllProductListCache();
        log.info("[Product#save] 写 MySQL rows={}, 已清理 Redis 列表/全量缓存", rows);
        return rows > 0;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product", key = "'all'"),
            @CacheEvict(value = "product", key = "#product.id")
    })
    public boolean update(Product product) {
        int rows = productMapper.update(product);
        evictAllProductListCache();
        log.info("[Product#update id={}] 写 MySQL rows={}, 已清理 Redis 列表/全量缓存",
                product.getId(), rows);
        return rows > 0;
    }

    @Override
    @CacheEvict(value = "product", key = "#id")
    public boolean deleteById(Integer id) {
        int rows = productMapper.deleteById(id);
        evictAllProductListCache();
        log.info("[Product#deleteById id={}] 写 MySQL rows={}, 已清理 Redis 列表/全量缓存",
                id, rows);
        return rows > 0;
    }

    /** 清理所有商品相关的列表缓存（all + 所有 search:*） */
    private void evictAllProductListCache() {
        Set<String> keys = redisTemplate.keys("smartshop:list:product:*");
        if (keys != null && !keys.isEmpty()) {
            Long deleted = redisTemplate.delete(keys);
            log.info("[Product#evictAllProductListCache] 删除 Redis keys={}, count={}",
                    keys.stream().sorted().collect(Collectors.toList()), deleted);
        }
    }
}