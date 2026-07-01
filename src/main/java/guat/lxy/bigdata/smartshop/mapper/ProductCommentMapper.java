package guat.lxy.bigdata.smartshop.mapper;

import guat.lxy.bigdata.smartshop.entity.ProductComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductCommentMapper {

    @Select("""
        SELECT c.*, u.username
        FROM product_comment c
        INNER JOIN t_user u ON c.user_id = u.id
        WHERE c.product_id = #{productId}
        ORDER BY c.create_time ASC
    """)
    List<ProductComment> findByProductId(@Param("productId") Integer productId);

    @Insert("INSERT INTO product_comment(product_id, user_id, parent_id, content, rating) VALUES(#{productId}, #{userId}, #{parentId}, #{content}, #{rating})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductComment comment);

    @Delete("DELETE FROM product_comment WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Delete("DELETE FROM product_comment WHERE parent_id = #{parentId}")
    int deleteByParentId(@Param("parentId") Integer parentId);

    @Select("SELECT c.*, u.username FROM product_comment c INNER JOIN t_user u ON c.user_id = u.id WHERE c.id = #{id}")
    ProductComment findById(@Param("id") Integer id);

    @Select("SELECT COUNT(*) FROM product_comment WHERE product_id = #{productId}")
    int countByProductId(@Param("productId") Integer productId);

    @Select("SELECT IFNULL(ROUND(AVG(rating), 1), 0) FROM product_comment WHERE product_id = #{productId} AND (parent_id IS NULL OR parent_id = 0) AND rating IS NOT NULL AND rating > 0")
    Double avgRatingByProductId(@Param("productId") Integer productId);

    // ========== 点赞相关 ==========

    @Select("SELECT COUNT(*) FROM comment_like WHERE comment_id = #{commentId}")
    int countLikes(@Param("commentId") Integer commentId);

    @Select("SELECT comment_id FROM comment_like WHERE user_id = #{userId} AND comment_id IN (${commentIds})")
    List<Integer> findLikedCommentIds(@Param("userId") Integer userId, @Param("commentIds") String commentIds);

    @Insert("INSERT INTO comment_like(comment_id, user_id) VALUES(#{commentId}, #{userId})")
    int insertLike(@Param("commentId") Integer commentId, @Param("userId") Integer userId);

    @Delete("DELETE FROM comment_like WHERE comment_id = #{commentId} AND user_id = #{userId}")
    int deleteLike(@Param("commentId") Integer commentId, @Param("userId") Integer userId);

    @Delete("DELETE FROM comment_like WHERE comment_id = #{commentId}")
    int deleteLikesByCommentId(@Param("commentId") Integer commentId);

    @Delete("""
        DELETE cl FROM comment_like cl
        INNER JOIN product_comment c ON cl.comment_id = c.id
        WHERE c.parent_id = #{parentId}
    """)
    int deleteLikesByParentId(@Param("parentId") Integer parentId);
}
