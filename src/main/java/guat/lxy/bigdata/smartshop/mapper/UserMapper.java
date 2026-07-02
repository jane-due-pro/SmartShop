package guat.lxy.bigdata.smartshop.mapper;

import guat.lxy.bigdata.smartshop.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM t_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Select("SELECT * FROM t_user WHERE email = #{email}")
    User findByEmail(@Param("email") String email);

    @Insert("INSERT INTO t_user(username, password, active, email) VALUES(#{username}, #{password}, #{active}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE t_user SET password = #{password} WHERE username = #{username}")
    int updatePassword(@Param("username") String username, @Param("password") String password);

    @Update("UPDATE t_user SET avatar = #{avatar} WHERE id = #{id}")
    int updateAvatar(@Param("id") Integer id, @Param("avatar") String avatar);

    @Update("UPDATE t_user SET email = #{email} WHERE id = #{id}")
    int updateEmail(@Param("id") Integer id, @Param("email") String email);
}
