package com.yingxue.lesson.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.subject.Subject;

public class ShiorAuthorizationDemo {
    private void test() {
        //构建SecurityManager环境        
        DefaultSecurityManager defaultSecurityManager=new DefaultSecurityManager();
        //创建一个SimpleAccountRealm 域        
        SimpleAccountRealm simpleAccountRealm=new SimpleAccountRealm();
        //添加一个测试账号、和所拥有的角色(后面可以做成读取动态读取数据库)        
        simpleAccountRealm.addAccount("zhangsan", "123456","admin","user");
        //设置Realm        
        defaultSecurityManager.setRealm(simpleAccountRealm);
        SecurityUtils.setSecurityManager(defaultSecurityManager);
        //获取主体      
        Subject subject=SecurityUtils.getSubject();
        //用户名和密码(用户输入的用户名密码)生成token        
        UsernamePasswordToken token=new UsernamePasswordToken("zhangsan", "123456");
        try {
            //进行登入(提交认证)            
            subject.login(token);
            subject.checkRoles("admin","user");
        }catch (IncorrectCredentialsException exception){
            System.out.println("用户名密码不匹配");
        }catch (LockedAccountException exception){
            System.out.println("账号已被锁定");
        }catch (DisabledAccountException exception){
            System.out.println("账号已被禁用");
        }catch (UnknownAccountException exception){
            System.out.println("用户不存在");
        }catch (UnauthorizedException ae) {
            System.out.println("用户没有权限");
        }
        System.out.println("用户认证的状态：isAuthenticated="+subject.isAuthenticated());
        //登出logout        
        System.out.println("执行 logout()方法后");
        subject.logout();
        System.out.println("用户认证的状态：isAuthenticated="+subject.isAuthenticated());
    }
}
