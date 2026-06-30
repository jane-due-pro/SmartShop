package guat.lxy.bigdata.smartshop.mapper;

import guat.lxy.bigdata.smartshop.entity.UserRole;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserRoleMapper {

    @Insert("INSERT INTO t_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserRole userRole);

    @Delete("DELETE FROM t_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);
}
