package guat.lxy.bigdata.smartshop.service;

import guat.lxy.bigdata.smartshop.entity.ProductComment;

import java.util.List;
import java.util.Map;

public interface ProductCommentService {

    /** 查商品评论（已组装父子结构） */
    List<ProductComment> findByProductId(Integer productId);

    /** 保存评论 */
    boolean save(ProductComment comment);

    /** 删除评论（ADMIN可删任意，普通用户只能删自己的） */
    boolean deleteById(Integer id, String currentUsername);

    /** 评论数 */
    int countByProductId(Integer productId);

    /** 平均评分 */
    Double avgRatingByProductId(Integer productId);

    /** 切换点赞状态，返回当前是否已点赞 */
    boolean toggleLike(Integer commentId, Integer userId);

    /** 填充评论的点赞数和当前用户点赞状态 */
    void fillLikeInfo(List<ProductComment> comments, Integer userId);
}
