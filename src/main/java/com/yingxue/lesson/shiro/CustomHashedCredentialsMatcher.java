package com.yingxue.lesson.shiro;

import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.service.RedisService;
import com.yingxue.lesson.utils.JwtTokenUtil;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;

/**
 * 自定义密码匹配器 认证的关键
 * CredentialsMatcher：密码加密/校验
 * 因为客户端首次登录后，后续的操作用户可以不在输入用户名密码，直接拿 token 凭证来验证用户，所以我们得改造一下 shiro 验证器，把它改造成验证 token 是否有效的业务逻辑。
 */
public class CustomHashedCredentialsMatcher extends HashedCredentialsMatcher {
    @Autowired
    private RedisService redisService;

    /**
     * 重写shiro核心比对认证方法
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        CustomUsernamePasswordToken customUsernamePasswordToken= (CustomUsernamePasswordToken) token;
        String accessToken= (String) customUsernamePasswordToken.getPrincipal();
        String userId= JwtTokenUtil.getUserId(accessToken);
        /**
         * 判断用户是否被锁定
         * 如果redis存在这个key，那么就是用户被锁定了
         */
        if(redisService.hasKey(Constant.ACCOUNT_LOCK_KEY+userId)){
            throw new BusinessException(BaseResponseCode.ACCOUNT_LOCK);
        }
        /**
         * 判断用户是否被删除
         * 我们数据库对用户表的删除用的是逻辑删除(is_deleted = 1/2)，而我们使用的jwt是无状态的，所以只能将被删除的用户，标记在redis里
         */
        if(redisService.hasKey(Constant.DELETED_USER_KEY+userId)){
            throw new BusinessException(BaseResponseCode.ACCOUNT_HAS_DELETED_ERROR);
        }

        /**
         * 判断token 是否主动登出
         * 用户主动退出后端会把 Contants.JWT_ACCESS_TOKEN_BLACKLIST+access_token 作为 key 存入redis 并且设置过期时间为 access_token 剩余的过期时间
         * 用户重新登录后会签发新的access_token
         */
        if(redisService.hasKey(Constant.JWT_ACCESS_TOKEN_BLACKLIST+accessToken)){
            throw new BusinessException(BaseResponseCode.TOKEN_ERROR);
        }
        /**
         * 判断token是否通过校验
         */
        if(!JwtTokenUtil.validateToken(accessToken)){
            throw new BusinessException(BaseResponseCode.TOKEN_PAST_DUE);
        }
        /**
         * 判断这个登录用户是否要主动去刷新
         *
         * 如果 key=Constant.JWT_REFRESH_KEY+userId大于accessToken说明是在 accessToken不是重新生成的
         * 这样就要判断它是否刷新过了/或者是否是新生成的token
         */
        /**
         * 因为jwt是无状态的，所以签发出去的token，无法管理，所以只能在修改的时候，用redis标识
         * 判断用户是否需要刷新(因为后台修改了用户所拥有的角色/菜单权限的时候需要把相关联用户都用redis标记起来(过期时间为access_token 生成的过期时间)，需要刷新access_token重新分配角色)
         * 但是需要排除重新登录的用户（重新登录的用户，已经签发了新的token，新token里包含新的权限信息），所以不需要重新刷新了，就要比较这个accessToken和Constant.JWT_REFRESH_KEY+userId两者的剩余过期时间。
         * 如果Constant.JWT_REFRESH_KEY+userId大于accessToken说明是在 accessToken不是重新生成的
         * 这样就要判断它是否刷新过了/或者是否是新生成的token
         */
        if(redisService.hasKey(Constant.JWT_REFRESH_KEY+userId)){
            /**
             * 通过剩余的过期时间比较如果token的剩余过期时间大与标记key的剩余过期时间
             * 就说明这个tokne是在这个标记key之后生成的
             */
            if(redisService.getExpire(Constant.JWT_REFRESH_KEY+userId, TimeUnit.MILLISECONDS)>JwtTokenUtil.getRemainingTime(accessToken)){
                throw new BusinessException(BaseResponseCode.TOKEN_PAST_DUE);
            }
        }
        return true;
    }
}
