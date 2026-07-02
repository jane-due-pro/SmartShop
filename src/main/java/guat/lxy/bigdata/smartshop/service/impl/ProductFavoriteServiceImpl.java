package guat.lxy.bigdata.smartshop.service.impl;

import guat.lxy.bigdata.smartshop.entity.Product;
import guat.lxy.bigdata.smartshop.mapper.ProductFavoriteMapper;
import guat.lxy.bigdata.smartshop.service.ProductFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductFavoriteServiceImpl implements ProductFavoriteService {

    @Autowired
    private ProductFavoriteMapper favoriteMapper;

    @Override
    public boolean toggle(Integer productId, Integer userId) {
        if (favoriteMapper.exists(productId, userId) > 0) {
            favoriteMapper.delete(productId, userId);
            return false; // 取消收藏
        }
        favoriteMapper.insert(productId, userId);
        return true; // 收藏成功
    }

    @Override
    public boolean isFavorited(Integer productId, Integer userId) {
        return favoriteMapper.exists(productId, userId) > 0;
    }

    @Override
    public int countByProductId(Integer productId) {
        return favoriteMapper.countByProductId(productId);
    }

    @Override
    public List<Product> findFavoritesByUserId(Integer userId) {
        return favoriteMapper.findFavoritesByUserId(userId);
    }
}
