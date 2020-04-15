package com.yingxue.lesson.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * jwt是基于token的认证原理
 * 且shiro本身用UsernamePasswordToken支持token
 * 所以我们改造Shiro的UsernamePasswordToken，使其适配我们所使用的jwt
 */
public class CustomUsernamePasswordToken extends UsernamePasswordToken {

    private String token;
    /**
     * 封装构造方法，使其支持传入我们的jwt
     * 以前shiro验证: UsernamePasswordToken token = new UsernamePasswordToken(username, password);用用户名密码构造一个token
     *                SecurityUtils.getSubject().login(token); 构造的token提交到安全管理器，然后流转到用户认证器进行用户认证
     */
    public CustomUsernamePasswordToken(String token) {
        this.token = token;
    }

    /**
     *  验证是需要使用getPrincipal，重写为返回我们的jwt作为验证凭证
     */
    @Override
    public Object getPrincipal() {
        return token;
    }
}
