package guat.lxy.bigdata.smartshop.config;

import guat.lxy.bigdata.smartshop.util.CacheHelper;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 缓存清理切面
 * 统一处理商品和分类的缓存清理
 */
@Aspect
@Component
public class CacheEvictAspect {

    @Autowired
    private CacheHelper cacheHelper;

    /**
     * 商品保存/更新/删除后清理缓存
     */
    @After("execution(* guat.lxy.bigdata.smartshop.service.impl.ProductServiceImpl.save(..)) || " +
           "execution(* guat.lxy.bigdata.smartshop.service.impl.ProductServiceImpl.update(..)) || " +
           "execution(* guat.lxy.bigdata.smartshop.service.impl.ProductServiceImpl.deleteById(..))")
    public void evictProductCache() {
        cacheHelper.evictByPattern("smartshop:list:product:*");
    }

    /**
     * 分类保存/更新/删除后清理缓存
     */
    @After("execution(* guat.lxy.bigdata.smartshop.service.impl.CategoryServiceImpl.save(..)) || " +
           "execution(* guat.lxy.bigdata.smartshop.service.impl.CategoryServiceImpl.update(..)) || " +
           "execution(* guat.lxy.bigdata.smartshop.service.impl.CategoryServiceImpl.deleteById(..))")
    public void evictCategoryCache() {
        cacheHelper.evict("smartshop:list:category:all");
    }
}
