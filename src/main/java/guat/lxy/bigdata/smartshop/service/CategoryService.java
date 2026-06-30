package guat.lxy.bigdata.smartshop.service;

import guat.lxy.bigdata.smartshop.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> findAll();
    Category findById(Integer id);
    boolean save(Category category);
    boolean update(Category category);
    boolean deleteById(Integer id);

    /** 编程式 Redis 缓存版的分类全量列表 */
    List<Category> findAllWithCache();
}
