package guat.lxy.bigdata.smartshop.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import guat.lxy.bigdata.smartshop.entity.Product;
import guat.lxy.bigdata.smartshop.mapper.ProductMapper;
import guat.lxy.bigdata.smartshop.service.ProductService;
import guat.lxy.bigdata.smartshop.util.SerializablePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商品服务实现。
 *
 * 缓存策略：
 * 1. 声明式（@Cacheable / @CacheEvict）：单商品查询 + 全量列表走 Redis "product" 命名空间
 * 2. 编程式（RedisTemplate）：分页条件查询走 Redis，key = smartshop:list:product:search:{条件哈希}
 *    写操作清除所有 smartshop:list:product:* 的 key
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

    // ===================== 声明式缓存 =====================

    @Override
    @Cacheable(value = "product", key = "'all'")
    public List<Product> findAll() {
        log.info("[Product#findAll] Cache MISS - query MySQL");
        return productMapper.findAllWithCategory();
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public Product findById(Integer id) {
        log.info("[Product#findById({})] Cache MISS - query MySQL", id);
        return productMapper.findByIdWithCategory(id);
    }

    // ===================== 编程式 Redis 缓存（列表） =====================

    @SuppressWarnings("unchecked")
    @Override
    public List<Product> findAllWithCache() {
        Object cached = redisTemplate.opsForValue().get(LIST_KEY_ALL);
        if (cached instanceof List) {
            log.info("[Product#findAllWithCache] Redis hit");
            return (List<Product>) cached;
        }
        log.info("[Product#findAllWithCache] Redis miss, query MySQL");
        List<Product> list = productMapper.findAllWithCategory();
        if (list != null) {
            redisTemplate.opsForValue().set(LIST_KEY_ALL, list, listTtlSeconds, TimeUnit.SECONDS);
        }
        return list;
    }

    @Override
    public PageInfo<Product> searchWithPage(Integer catId, String name,
                                            Double minPrice, Double maxPrice,
                                            Integer pageNum, Integer pageSize) {
        String cacheKey = buildSearchKey(catId, name, minPrice, maxPrice, pageNum, pageSize);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof SerializablePage) {
            log.info("[Product#searchWithPage] Redis hit, key={}", cacheKey);
            return ((SerializablePage<Product>) cached).toPageInfo();
        }

        log.info("[Product#searchWithPage] Redis miss, key={}, query MySQL", cacheKey);
        PageHelper.startPage(pageNum, pageSize);
        List<Product> list = productMapper.searchWithCondition(catId, name, minPrice, maxPrice);
        PageInfo<Product> pageInfo = new PageInfo<>(list);

        redisTemplate.opsForValue().set(cacheKey, SerializablePage.of(pageInfo),
                listTtlSeconds, TimeUnit.SECONDS);
        log.info("[Product#searchWithPage] written to Redis, key={}, total={}", cacheKey, pageInfo.getTotal());
        return pageInfo;
    }

    private String buildSearchKey(Integer catId, String name, Double minPrice,
                                  Double maxPrice, Integer pageNum, Integer pageSize) {
        String safeName = name == null ? "" : name.replace('|', '_');
        return LIST_KEY_SEARCH_PREFIX
                + "c=" + (catId == null ? "a" : catId)
                + "|n=" + safeName
                + "|min=" + (minPrice == null ? "0" : minPrice)
                + "|max=" + (maxPrice == null ? "inf" : maxPrice)
                + "|p=" + pageNum
                + "|s=" + pageSize;
    }

    // ===================== 写操作 =====================

    @Override
    @CacheEvict(value = "product", allEntries = true)
    public boolean save(Product product) {
        int rows = productMapper.insert(product);
        evictAllProductListCache();
        log.info("[Product#save] rows={}, cache evicted", rows);
        return rows > 0;
    }

    @Override
    @CacheEvict(value = "product", allEntries = true)
    public boolean update(Product product) {
        int rows = productMapper.update(product);
        evictAllProductListCache();
        log.info("[Product#update id={}] rows={}, cache evicted", product.getId(), rows);
        return rows > 0;
    }

    @Override
    @CacheEvict(value = "product", allEntries = true)
    public boolean deleteById(Integer id) {
        int rows = productMapper.deleteById(id);
        evictAllProductListCache();
        log.info("[Product#deleteById id={}] rows={}, cache evicted", id, rows);
        return rows > 0;
    }

    private void evictAllProductListCache() {
        Set<String> keys = redisTemplate.keys("smartshop:list:product:*");
        if (keys != null && !keys.isEmpty()) {
            Long deleted = redisTemplate.delete(keys);
            log.info("[Product#evictAllProductListCache] deleted {} keys: {}", deleted,
                    keys.stream().sorted().collect(Collectors.toList()));
        }
    }
}
