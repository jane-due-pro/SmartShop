package guat.lxy.bigdata.smartshop.service.impl;

import guat.lxy.bigdata.smartshop.entity.Role;
import guat.lxy.bigdata.smartshop.entity.User;
import guat.lxy.bigdata.smartshop.entity.UserRole;
import guat.lxy.bigdata.smartshop.mapper.RoleMapper;
import guat.lxy.bigdata.smartshop.mapper.UserMapper;
import guat.lxy.bigdata.smartshop.mapper.UserRoleMapper;
import guat.lxy.bigdata.smartshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

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
        User existing = userMapper.findByUsername(user.getUsername());
        if (existing != null) {
            return false;
        }
        user.setActive(1);
        user.setPassword("{noop}" + user.getPassword());
        userMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(2);
        userRoleMapper.insert(userRole);

        return true;
    }

    @Override
    @Transactional
    public boolean resetPassword(String email, String code, String newPassword) {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            return false;
        }
        userMapper.updatePassword(user.getUsername(), "{noop}" + newPassword);
        return true;
    }

    @Override
    public boolean updateEmail(Integer userId, String email) {
        User existing = userMapper.findByEmail(email);
        if (existing != null) {
            return false;
        }
        userMapper.updateEmail(userId, email);
        return true;
    }

    @Override
    public List<Role> getUserRoles(Integer userId) {
        return roleMapper.findByUserId(userId);
    }
}
