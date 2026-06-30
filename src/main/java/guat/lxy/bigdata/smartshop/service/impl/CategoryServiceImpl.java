package guat.lxy.bigdata.smartshop.service.impl;

import guat.lxy.bigdata.smartshop.entity.Category;
import guat.lxy.bigdata.smartshop.mapper.CategoryMapper;
import guat.lxy.bigdata.smartshop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "category")
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @Cacheable(key = "'all'")
    public List<Category> findAll() {
        return categoryMapper.findAll();
    }

    @Override
    @Cacheable(key = "#id")
    public Category findById(Integer id) {
        return categoryMapper.findById(id);
    }

    @Override
    @CacheEvict(key = "'all'")
    public boolean save(Category category) {
        categoryMapper.insert(category);
        return true;
    }

    @Override
    @CacheEvict(key = "#category.id")
    public boolean update(Category category) {
        categoryMapper.update(category);
        return true;
    }

    @Override
    @CacheEvict(key = "#id")
    public boolean deleteById(Integer id) {
        categoryMapper.deleteById(id);
        return true;
    }
}
