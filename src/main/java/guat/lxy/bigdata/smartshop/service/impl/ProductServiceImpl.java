package guat.lxy.bigdata.smartshop.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import guat.lxy.bigdata.smartshop.entity.Product;
import guat.lxy.bigdata.smartshop.mapper.ProductMapper;
import guat.lxy.bigdata.smartshop.service.ProductService;
import guat.lxy.bigdata.smartshop.util.CacheHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private static final String KEY_ALL = "smartshop:list:product:all";
    private static final String KEY_BY_ID = "smartshop:cache:product:id:";
    private static final String KEY_SEARCH_PREFIX = "smartshop:list:product:search:";

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CacheHelper cacheHelper;

    @Value("${smartshop.cache.ttl.product-list}")
    private long ttlSeconds;

    @Override
    public List<Product> findAll() {
        return cacheHelper.getOrLoad(KEY_ALL, ttlSeconds, () -> productMapper.findAllWithCategory());
    }

    @Override
    public Product findById(Integer id) {
        return cacheHelper.getOrLoad(KEY_BY_ID + id, ttlSeconds, () -> productMapper.findByIdWithCategory(id));
    }

    @Override
    public PageInfo<Product> searchWithPage(Integer catId, String name,
                                            Double minPrice, Double maxPrice,
                                            Integer pageNum, Integer pageSize) {
        String cacheKey = buildSearchKey(catId, name, minPrice, maxPrice, pageNum, pageSize);
        return cacheHelper.getOrLoadPage(cacheKey, ttlSeconds, () -> {
            PageHelper.startPage(pageNum, pageSize);
            List<Product> list = productMapper.searchWithCondition(catId, name, minPrice, maxPrice);
            return new PageInfo<>(list);
        });
    }

    private String buildSearchKey(Integer catId, String name, Double minPrice,
                                  Double maxPrice, Integer pageNum, Integer pageSize) {
        return String.format("%sc=%d:n=%s:min=%.2f:max=%.2f:p=%d:s=%d",
                KEY_SEARCH_PREFIX,
                catId != null ? catId : 0,
                name != null ? name : "",
                minPrice != null ? minPrice : 0,
                maxPrice != null ? maxPrice : Double.MAX_VALUE,
                pageNum,
                pageSize);
    }

    @Override
    public boolean save(Product product) {
        return productMapper.insert(product) > 0;
    }

    @Override
    public boolean update(Product product) {
        cacheHelper.evict(KEY_BY_ID + product.getId());
        return productMapper.update(product) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        cacheHelper.evict(KEY_BY_ID + id);
        return productMapper.deleteById(id) > 0;
    }
}