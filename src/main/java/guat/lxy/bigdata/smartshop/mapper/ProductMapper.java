package guat.lxy.bigdata.smartshop.mapper;

import guat.lxy.bigdata.smartshop.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Results(id = "productWithCategory", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "photoUrl", column = "photo_url"),
        @Result(property = "price", column = "price"),
        @Result(property = "descp", column = "descp"),
        @Result(property = "releaseDate", column = "release_date"),
        @Result(property = "catId", column = "cat_id"),
        @Result(property = "categoryName", column = "categoryName")
    })
    @Select("""
        SELECT p.*, c.name AS categoryName 
        FROM product p 
        INNER JOIN category c ON p.cat_id = c.id 
        ORDER BY p.id DESC
    """)
    List<Product> findAllWithCategory();

    @Select("""
        SELECT p.*, c.name AS categoryName 
        FROM product p 
        INNER JOIN category c ON p.cat_id = c.id 
        WHERE p.id = #{id}
    """)
    Product findByIdWithCategory(@Param("id") Integer id);

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product findById(@Param("id") Integer id);

    @Insert("INSERT INTO product(name, photo_url, price, descp, release_date, cat_id) VALUES(#{name}, #{photoUrl}, #{price}, #{descp}, #{releaseDate}, #{catId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE product SET name=#{name}, photo_url=#{photoUrl}, price=#{price}, descp=#{descp}, release_date=#{releaseDate}, cat_id=#{catId} WHERE id=#{id}")
    int update(Product product);

    @Delete("DELETE FROM product WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    // 动态条件查询
    @Select("""
        <script>
        SELECT p.*, c.name AS categoryName 
        FROM product p 
        INNER JOIN category c ON p.cat_id = c.id 
        <where>
            <if test="catId != null"> AND p.cat_id = #{catId}</if>
            <if test="name != null and name != ''"> AND p.name LIKE CONCAT('%', #{name}, '%')</if>
            <if test="minPrice != null"> AND p.price >= #{minPrice}</if>
            <if test="maxPrice != null"> AND p.price &lt;= #{maxPrice}</if>
        </where>
        ORDER BY p.id DESC
        </script>
    """)
    List<Product> searchWithCondition(@Param("catId") Integer catId,
                                      @Param("name") String name,
                                      @Param("minPrice") Double minPrice,
                                      @Param("maxPrice") Double maxPrice);
}
