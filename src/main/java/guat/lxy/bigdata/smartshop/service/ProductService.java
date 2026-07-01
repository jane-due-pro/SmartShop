package guat.lxy.bigdata.smartshop.service;

import com.github.pagehelper.PageInfo;
import guat.lxy.bigdata.smartshop.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> findAll();
    Product findById(Integer id);
    boolean save(Product product);
    boolean update(Product product);
    boolean deleteById(Integer id);
    PageInfo<Product> searchWithPage(Integer catId, String name, Double minPrice, Double maxPrice, Integer pageNum, Integer pageSize);
}