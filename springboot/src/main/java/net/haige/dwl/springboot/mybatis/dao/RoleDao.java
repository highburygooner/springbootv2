package net.haige.dwl.springboot.mybatis.dao;

import net.haige.dwl.springboot.mybatis.model.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleDao {

    public Role get_role_by_id(int id);

}
