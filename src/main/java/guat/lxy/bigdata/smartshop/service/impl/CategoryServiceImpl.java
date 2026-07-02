package guat.lxy.bigdata.smartshop.service.impl;

import guat.lxy.bigdata.smartshop.entity.Category;
import guat.lxy.bigdata.smartshop.mapper.CategoryMapper;
import guat.lxy.bigdata.smartshop.service.CategoryService;
import guat.lxy.bigdata.smartshop.util.CacheHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final String KEY_ALL = "smartshop:list:category:all";
    private static final String KEY_BY_ID = "smartshop:cache:category:id:";

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CacheHelper cacheHelper;

    @Value("${smartshop.cache.ttl.category-list}")
    private long ttlSeconds;

    @Override
    public List<Category> findAll() {
        return cacheHelper.getOrLoad(KEY_ALL, ttlSeconds, () -> categoryMapper.findAll());
    }

    @Override
    public Category findById(Integer id) {
        return cacheHelper.getOrLoad(KEY_BY_ID + id, ttlSeconds, () -> categoryMapper.findById(id));
    }

    @Override
    public boolean save(Category category) {
        return categoryMapper.insert(category) > 0;
    }

    @Override
    public boolean update(Category category) {
        cacheHelper.evict(KEY_BY_ID + category.getId());
        return categoryMapper.update(category) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        cacheHelper.evict(KEY_BY_ID + id);
        return categoryMapper.deleteById(id) > 0;
    }
}