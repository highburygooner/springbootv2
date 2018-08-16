package net.haige.dwl.springboot.mybatis.services.impl;

import net.haige.dwl.springboot.mybatis.dao.UserLoginDao;
import net.haige.dwl.springboot.mybatis.model.User;
import net.haige.dwl.springboot.mybatis.services.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "LoginService")
public class LoginServiceImpl implements LoginService {
    @Autowired
    private UserLoginDao loginDao;//这里会报错，但是并不会影响
    @Override
    public User getUser(String username) {
//        // 没有此用户直接返回null
//        if (! DataSource.getData().containsKey(username))
//            return null;
//
//        UserBean user = new UserBean();
//        Map<String, String> detail = DataSource.getData().get(username);
//
//        user.setUsername(username);
//        user.setPassword(detail.get("password"));
//        user.setRole(detail.get("role"));
//        user.setPermission(detail.get("permission"));
//        return user;


        return  loginDao.get_user_by_name(username);
    }


}
