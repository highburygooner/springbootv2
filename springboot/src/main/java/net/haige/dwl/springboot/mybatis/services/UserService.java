package net.haige.dwl.springboot.mybatis.services;

import net.haige.dwl.springboot.mybatis.model.MovieUser;

public interface UserService {


    /**
     * @Author: dwl
     * @Description: 更改用户信息
     * @Date: 2018/4/20
     * @param user
     */
    int updateUserInfo(MovieUser user) throws Exception;

    /**
     * @Author: dwl
     * @Description: 根据主键编号获取用户信息
     * @Date: 2018/4/20
     * @param uId 主键
     */
    MovieUser getUserInfoById(Integer uId);

}
