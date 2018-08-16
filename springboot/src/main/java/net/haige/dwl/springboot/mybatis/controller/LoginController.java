package net.haige.dwl.springboot.mybatis.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.haige.dwl.springboot.mybatis.model.ResponseBean;
import net.haige.dwl.springboot.mybatis.model.User;
import net.haige.dwl.springboot.mybatis.services.LoginService;
import net.haige.dwl.util.JWTUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@Api(value = "demo演示", description = "主要提供演示测试相关的接口API")
public class LoginController {


    private static final Logger LOGGER = LogManager.getLogger(UserController.class);
    @Autowired
    private LoginService service;


    @PostMapping("/login")
    @ApiOperation(value="用户登录", notes="")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String")}
          )
    public ResponseBean login(@RequestParam("username") String username,
                              @RequestParam("password") String password) {
        User userBean = service.getUser(username);
        if (userBean.getPassword().equals(password)) {
            //返回json数据
            return new ResponseBean(200, "Login success", JWTUtil.sign(username, password));
        } else {
            throw new UnauthorizedException();
        }
    }

    @GetMapping("/edit")
    public ResponseBean edit() {
        return new ResponseBean(200, "You are editing now", null);
    }

    @GetMapping("/admin/hello")
    public ResponseBean adminView() {
        return new ResponseBean(200, "You are visiting admin content", null);
    }


    @RequestMapping(path = "/401")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseBean unauthorized() {
        return new ResponseBean(401, "Unauthorized", null);
    }

//    @GetMapping("/annotation/require_auth")
//    @RequiresAuthentication
//    public ResponseBean annotationView1() {
//        return new ResponseBean(200, "You are visiting require_auth", null);
//    }
//
//    @GetMapping("/annotation/require_role")
//  //  @RequiresRoles("admin")
//    public ResponseBean annotationView2() {
//        return new ResponseBean(200, "You are visiting require_role", null);
//    }

//    @GetMapping("/annotation/require_permission")
//    @RequiresPermissions(logical = Logical.AND, value = {"view", "edit"})
//    public ResponseBean annotationView3() {
//        return new ResponseBean(200, "You are visiting permission require edit,view", null);
//    }
//

//    @RequestMapping(path = "/401")
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public ResponseBean unauthorized() {
//        return new ResponseBean(401, "Unauthorized", null);
//    }

}
