package guat.lxy.bigdata.smartshop.mapper;

import guat.lxy.bigdata.smartshop.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {

    @Select("SELECT r.* FROM t_role r INNER JOIN t_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<Role> findByUserId(@Param("userId") Integer userId);
}
