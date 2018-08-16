package net.haige.dwl.springboot.mybatis.dao;

import net.haige.dwl.springboot.mybatis.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserLoginDao {


    public User get_user_by_name(String name);

}
