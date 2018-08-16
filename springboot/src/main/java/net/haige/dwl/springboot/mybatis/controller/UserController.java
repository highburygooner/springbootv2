package net.haige.dwl.springboot.mybatis.controller;

import net.haige.dwl.springboot.mybatis.model.MovieUser;
import net.haige.dwl.springboot.mybatis.model.ResponseBean;
import net.haige.dwl.springboot.mybatis.model.User;
import net.haige.dwl.springboot.mybatis.model.UserRole;
import net.haige.dwl.springboot.mybatis.services.LoginService;
import net.haige.dwl.springboot.mybatis.services.UserRoleService;
import net.haige.dwl.springboot.mybatis.services.UserService;
import net.haige.dwl.util.JWTUtil;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserService userRepository;
//    @Autowired
//    private LoginService service;
    @GetMapping("/{id}")
    public MovieUser findById(@PathVariable int id) {

        MovieUser findOne = this.userRepository.getUserInfoById(id);
        return findOne;
    }
//    @GetMapping("/role")
//    public ResponseBean getuserrole(@RequestParam("username") String username) {
//        UserRole userRole = userRoleService.get_user_role(username);
//      //  if (userBean.getPassword().equals(password)) {
//            //返回json数据
//            return new ResponseBean(200, "Login success", userRole);
//       // }
//    }


}
