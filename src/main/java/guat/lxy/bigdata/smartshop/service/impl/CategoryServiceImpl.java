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

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private static final String LIST_KEY_ALL = "smartshop:list:category:all";

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${smartshop.cache.ttl.category-list}")
    private long listTtlSeconds;

    @Override
    @Cacheable(value = "category", key = "'all'")
    public List<Category> findAll() {
        log.info("[Category#findAll] Cache MISS - query MySQL");
        return categoryMapper.findAll();
    }

    @Override
    @Cacheable(value = "category", key = "#id")
    public Category findById(Integer id) {
        log.info("[Category#findById({})] Cache MISS - query MySQL", id);
        return categoryMapper.findById(id);
    }

    @Override
    @CacheEvict(value = "category", allEntries = true)
    public boolean save(Category category) {
        int rows = categoryMapper.insert(category);
        redisTemplate.delete(LIST_KEY_ALL);
        log.info("[Category#save] rows={}, cache evicted", rows);
        return rows > 0;
    }

    @Override
    @CacheEvict(value = "category", allEntries = true)
    public boolean update(Category category) {
        int rows = categoryMapper.update(category);
        redisTemplate.delete(LIST_KEY_ALL);
        log.info("[Category#update id={}] rows={}, cache evicted", category.getId(), rows);
        return rows > 0;
    }

    @Override
    @CacheEvict(value = "category", allEntries = true)
    public boolean deleteById(Integer id) {
        int rows = categoryMapper.deleteById(id);
        redisTemplate.delete(LIST_KEY_ALL);
        log.info("[Category#deleteById id={}] rows={}, cache evicted", id, rows);
        return rows > 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Category> findAllWithCache() {
        Object cached = redisTemplate.opsForValue().get(LIST_KEY_ALL);
        if (cached instanceof List) {
            log.info("[Category#findAllWithCache] Redis hit");
            return (List<Category>) cached;
        }
        log.info("[Category#findAllWithCache] Redis miss, query MySQL");
        List<Category> list = categoryMapper.findAll();
        if (list != null) {
            redisTemplate.opsForValue().set(LIST_KEY_ALL, list, listTtlSeconds, TimeUnit.SECONDS);
        }
        return list;
    }
}
