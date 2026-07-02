package guat.lxy.bigdata.smartshop.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductFavoriteMapper {

    @Insert("INSERT INTO product_favorite(product_id, user_id) VALUES(#{productId}, #{userId})")
    int insert(@Param("productId") Integer productId, @Param("userId") Integer userId);

    @Delete("DELETE FROM product_favorite WHERE product_id = #{productId} AND user_id = #{userId}")
    int delete(@Param("productId") Integer productId, @Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM product_favorite WHERE product_id = #{productId} AND user_id = #{userId}")
    int exists(@Param("productId") Integer productId, @Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM product_favorite WHERE product_id = #{productId}")
    int countByProductId(@Param("productId") Integer productId);

    @Select("""
        SELECT p.id, p.name, p.photo_url, p.price, p.cat_id, c.name AS categoryName
        FROM product_favorite f
        INNER JOIN product p ON f.product_id = p.id
        INNER JOIN category c ON p.cat_id = c.id
        WHERE f.user_id = #{userId}
        ORDER BY f.create_time DESC
    """)
    List<guat.lxy.bigdata.smartshop.entity.Product> findFavoritesByUserId(@Param("userId") Integer userId);
}
