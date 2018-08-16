package net.haige.dwl.springboot.mybatis.model;

public class UserRole {

    private User user;//一个用户角色对应一个用户
    private Role role;//一个用户角色对应一个角色


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
