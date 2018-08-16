package net.haige.dwl.springboot.mybatis.dao;

import net.haige.dwl.springboot.mybatis.model.MovieUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface UserDao {

    /**
     * @Author: dwl
     * @Description: 更改用户信息
     * @Date: 2018/4/20
     * @param user
     */
    int updateUserInfo(MovieUser user);

    /**
     * @Author: dwl
     * @Description: 根据主键编号获取用户信息
     * @Date: 2018/4/20
     * @param uId 主键
     */
    MovieUser getUserInfoByIds(@Param("id") Integer uId);

}
