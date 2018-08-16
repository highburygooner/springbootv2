package net.haige.dwl.springboot.mybatis.services;

import net.haige.dwl.springboot.mybatis.model.User;

public interface LoginService {


    public User getUser(String username);

}
