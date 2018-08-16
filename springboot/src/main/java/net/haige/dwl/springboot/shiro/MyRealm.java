package net.haige.dwl.springboot.shiro;

import net.haige.dwl.springboot.jwt.JWTToken;
import net.haige.dwl.springboot.listener.SpringInitListener;
import net.haige.dwl.springboot.mybatis.model.User;
import net.haige.dwl.springboot.mybatis.model.UserRole;
import net.haige.dwl.springboot.mybatis.services.LoginService;
import net.haige.dwl.springboot.mybatis.services.UserRoleService;
import net.haige.dwl.util.JWTUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class MyRealm extends AuthorizingRealm {


    private static final Logger LOGGER = LogManager.getLogger(MyRealm.class);

   // private LoginService service;

 //   public static  MyRealm realm;

//    @PostConstruct
//    public void initial(){
//        realm=this;
//        realm.service=this.service;
//    }

    MyRealm() {
       // service = (LoginService)SpringInitListener.getApplicationContext().getBean("LoginService");
    }

    private LoginService getLoginService(){
        return  (LoginService)SpringInitListener.getApplicationContext().getBean("LoginService");
    }

    private UserRoleService getUserRoleService(){
        return  (UserRoleService)SpringInitListener.getApplicationContext().getBean("UserRoleService");
    }


    /**
     * 大坑！，必须重写此方法，不然Shiro会报错
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    /**
     * 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission之类的
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
          String username = JWTUtil.getUsername(principals.toString());
          UserRole userRole=getUserRoleService().get_user_role(username);

          SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
          simpleAuthorizationInfo.addRole(userRole.getRole().getRolename());
//        Set<String> permission = new HashSet<>(Arrays.asList(user.getPermission().split(",")));
//        simpleAuthorizationInfo.addStringPermissions(permission);//重要之处需要配成url
//        simpleAuthorizationInfo.addStringPermission("/annotation/require_permission");//重要之处需要配成url
//
        return simpleAuthorizationInfo;
    }

    /**
     * 默认使用此方法进行用户名正确与否验证，错误抛出异常即可。
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getCredentials();
        // 解密获得username，用于和数据库进行对比
        String username = JWTUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("token invalid");
        }

        User userBean = getLoginService().getUser(username);
        if (userBean == null) {
            throw new AuthenticationException("User didn't existed!");
        }
        if (! JWTUtil.verify(token, username, userBean.getPassword())) {
            throw new AuthenticationException("Username or password error");
        }
        return new SimpleAuthenticationInfo(token, token, "my_realm");
    }

}
