package net.haige.dwl.springboot.mybatis.dao;

import net.haige.dwl.springboot.mybatis.model.UserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleDao {


    public UserRole get_role_by_id(String id);

}
