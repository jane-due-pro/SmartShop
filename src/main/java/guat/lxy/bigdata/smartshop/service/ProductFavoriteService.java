package guat.lxy.bigdata.smartshop.service;

import guat.lxy.bigdata.smartshop.entity.Product;

import java.util.List;
import java.util.Map;

public interface ProductFavoriteService {

    /** 切换收藏状态，返回 true=已收藏 false=已取消 */
    boolean toggle(Integer productId, Integer userId);

    /** 当前用户是否已收藏该商品 */
    boolean isFavorited(Integer productId, Integer userId);

    /** 商品被收藏总数 */
    int countByProductId(Integer productId);

    /** 查询用户的收藏商品列表 */
    List<Product> findFavoritesByUserId(Integer userId);
}
