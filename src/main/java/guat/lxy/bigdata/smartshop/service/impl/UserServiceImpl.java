package guat.lxy.bigdata.smartshop.service.impl;

import guat.lxy.bigdata.smartshop.entity.Role;
import guat.lxy.bigdata.smartshop.entity.User;
import guat.lxy.bigdata.smartshop.entity.UserRole;
import guat.lxy.bigdata.smartshop.mapper.RoleMapper;
import guat.lxy.bigdata.smartshop.mapper.UserMapper;
import guat.lxy.bigdata.smartshop.mapper.UserRoleMapper;
import guat.lxy.bigdata.smartshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final int ROLE_USER_ID = 2;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    public User findById(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    @Transactional
    public boolean register(User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            return false;
        }
        user.setActive(1);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(ROLE_USER_ID);
        userRoleMapper.insert(userRole);

        return true;
    }

    @Override
    @Transactional
    public boolean resetPassword(String email, String newPassword) {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            return false;
        }
        userMapper.updatePassword(user.getUsername(), passwordEncoder.encode(newPassword));
        return true;
    }

    @Override
    public List<Role> getUserRoles(Integer userId) {
        return roleMapper.findByUserId(userId);
    }
}
