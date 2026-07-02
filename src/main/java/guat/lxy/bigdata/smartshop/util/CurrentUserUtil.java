package guat.lxy.bigdata.smartshop.util;

import guat.lxy.bigdata.smartshop.entity.User;
import guat.lxy.bigdata.smartshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * 当前用户工具类
 * 统一获取当前登录用户信息
 */
@Component
public class CurrentUserUtil {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户
     * @param principal 安全上下文中的Principal
     * @return 用户对象
     */
    public User getCurrentUser(Principal principal) {
        return userService.findByUsername(principal.getName());
    }

    /**
     * 获取当前登录用户的ID
     * @param principal 安全上下文中的Principal
     * @return 用户ID
     */
    public Integer getCurrentUserId(Principal principal) {
        return getCurrentUser(principal).getId();
    }
}
