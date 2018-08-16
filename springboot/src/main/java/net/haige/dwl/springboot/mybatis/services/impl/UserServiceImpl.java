package net.haige.dwl.springboot.mybatis.services.impl;

import net.haige.dwl.springboot.mybatis.dao.UserDao;
import net.haige.dwl.springboot.mybatis.model.MovieUser;
import net.haige.dwl.springboot.mybatis.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;//这里会报错，但是并不会影响


    @Transactional
    @Override
    public int updateUserInfo(MovieUser user) throws Exception {
        if (null == user.getId())
            throw new Exception("");
        return userDao.updateUserInfo(user);
    }

    @Override
    public MovieUser getUserInfoById(Integer uId) {
        return userDao.getUserInfoByIds(uId);
    }



}
