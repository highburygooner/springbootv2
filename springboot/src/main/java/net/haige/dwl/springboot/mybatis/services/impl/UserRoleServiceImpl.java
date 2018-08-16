package net.haige.dwl.springboot.mybatis.services.impl;

import net.haige.dwl.springboot.mybatis.dao.UserRoleDao;
import net.haige.dwl.springboot.mybatis.model.UserRole;
import net.haige.dwl.springboot.mybatis.services.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "UserRoleService")
public class UserRoleServiceImpl implements UserRoleService {

    @Autowired
    private UserRoleDao userRoleDao;
    @Override
    public UserRole get_user_role(String name) {
        return userRoleDao.get_role_by_id(name);
    }
}
