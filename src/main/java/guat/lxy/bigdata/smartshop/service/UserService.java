package guat.lxy.bigdata.smartshop.service;

import guat.lxy.bigdata.smartshop.entity.Role;
import guat.lxy.bigdata.smartshop.entity.User;
import java.util.List;

public interface UserService {
    User findByUsername(String username);
    User findByEmail(String email);
    User findById(Integer id);
    boolean register(User user);
    boolean resetPassword(String email, String code, String newPassword);
    boolean updateEmail(Integer userId, String email);
    List<Role> getUserRoles(Integer userId);
}
