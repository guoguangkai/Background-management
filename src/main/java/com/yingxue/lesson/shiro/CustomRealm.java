package com.yingxue.lesson.shiro;

import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.service.PermissionService;
import com.yingxue.lesson.service.RedisService;
import com.yingxue.lesson.service.RoleService;
import com.yingxue.lesson.service.UserService;
import com.yingxue.lesson.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 自定义域
 */
public class CustomRealm extends AuthorizingRealm {
    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RedisService redisService;
    /**
     * 重写supports 不然token不生效
     * 返回当前领域是否支持参数中的token。只有当前Realm支持这个类型的token时，Shiro才会使用这个类型的token调用 getAuthenticationInfo(AuthenticationToken token)方法进行身份认证。
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof CustomUsernamePasswordToken;
    }
    /**
     * 自定义 Realm   【从jwt中拿数据给授权/验证器】
     * 主要是继承 AuthorizingRealm 实现两个比较关键的方法 doGetAuthorizationInfo(主要用于用户授权，就是设置用户所拥有的角色/权限)、
     * doGetAuthenticationInfo(主要用户用户的认证，以前是验证用户名密码这里我们会改造成验证 token 一般来说客户端只需登录一次后续的访问用 token来维护登录的状态，所以我们这里改造成验证 token)
     */
    /**
     * 该方法主要是用于当前登录用户授权。用户授权时调用
     * 1. 调用SecurityUtils.getSubject().isPermitted(String str)方法时会调用。
     * 2. 在@Controller 上@RequiresRoles("admin")在方法上加注解的时候调用
     * 3. [@shiro.hasPermission name = "admin"][/@shiro.hasPermission]或者<shiro:hasPermission name="admin"></shiro:hasPermission>在页面上加shiro标签的时候，即进这个页面的时候扫描到有这个标签的时候调用
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        //拿到token
        String accessToken= (String) principalCollection.getPrimaryPrincipal();
        SimpleAuthorizationInfo info=new SimpleAuthorizationInfo();
        String userId=JwtTokenUtil.getUserId(accessToken);
        /**
         * 通过剩余的过期时间比较如果token的剩余过期时间大与标记key的剩余过期时间
         * 就说明这个tokne是在这个标记key之后生成的
         */
        if(redisService.hasKey(Constant.JWT_REFRESH_KEY+userId)&&redisService.getExpire(Constant.JWT_REFRESH_KEY+userId, TimeUnit.MILLISECONDS)>JwtTokenUtil.getRemainingTime(accessToken)){
            List<String> roleNames = roleService.getRoleNames(userId);
            if(roleNames!=null&&!roleNames.isEmpty()){
                //将角色信息加入到SimpleAuthorizationInfo中，就不用再查数据库了
                info.addRoles(roleNames);
            }
            Set<String> permissions=permissionService.getPermissionsByUserId(userId);
            if(permissions!=null){
                //将权限信息加入到SimpleAuthorizationInfo中，就不用再查数据库了
                info.addStringPermissions(permissions);
            }
        }else {
            Claims claims= JwtTokenUtil.getClaimsFromToken(accessToken);
            if(claims.get(Constant.JWT_ROLES_KEY)!=null){
                info.addRoles((Collection<String>) claims.get(Constant.JWT_ROLES_KEY));
            }
            if(claims.get(Constant.JWT_PERMISSIONS_KEY)!=null){
                info.addStringPermissions((Collection<String>) claims.get(Constant.JWT_PERMISSIONS_KEY));
            }
        }
        //返回该用户的权限和角色信息给授权器
        return info;
    }

    /**
     * GetAuthenticationInfo
     * 用户验证时调用。
     * 调用currUser.login(token)方法时会调用doGetAuthenticationInfo方法。
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //获取我们的jwt，将jwt中的用户信息封装为SimpleAuthenticationInfo，返回给认证器，就不用再查数据库了
        CustomUsernamePasswordToken customUsernamePasswordToken= (CustomUsernamePasswordToken) authenticationToken;
        SimpleAuthenticationInfo info=new SimpleAuthenticationInfo(customUsernamePasswordToken.getPrincipal(),customUsernamePasswordToken.getCredentials(),CustomRealm.class.getName());
        return info;
    }
}
