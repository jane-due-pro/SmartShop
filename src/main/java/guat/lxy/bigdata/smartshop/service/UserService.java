package guat.lxy.bigdata.smartshop.service;

import guat.lxy.bigdata.smartshop.entity.Role;
import guat.lxy.bigdata.smartshop.entity.User;
import java.util.List;

public interface UserService {
    User findByUsername(String username);
    User findByEmail(String email);
    boolean register(User user);
    boolean resetPassword(String email, String newPassword);
    List<Role> getUserRoles(Integer userId);
}

