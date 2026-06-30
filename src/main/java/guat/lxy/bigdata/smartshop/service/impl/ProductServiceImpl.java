package guat.lxy.bigdata.smartshop.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import guat.lxy.bigdata.smartshop.entity.Product;
import guat.lxy.bigdata.smartshop.mapper.ProductMapper;
import guat.lxy.bigdata.smartshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "product")
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Cacheable(key = "'all'")
    public List<Product> findAll() {
        return productMapper.findAllWithCategory();
    }

    @Override
    @Cacheable(key = "#id")
    public Product findById(Integer id) {
        return productMapper.findByIdWithCategory(id);
    }

    @Override
    @CacheEvict(key = "'all'")
    public boolean save(Product product) {
        productMapper.insert(product);
        return true;
    }

    @Override
    @CacheEvict(key = "#product.id")
    public boolean update(Product product) {
        productMapper.update(product);
        return true;
    }

    @Override
    @CacheEvict(key = "#id")
    public boolean deleteById(Integer id) {
        productMapper.deleteById(id);
        return true;
    }

    @Override
    public PageInfo<Product> searchWithPage(Integer catId, String name, Double minPrice, Double maxPrice, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Product> list = productMapper.searchWithCondition(catId, name, minPrice, maxPrice);
        return new PageInfo<>(list);
    }
}
