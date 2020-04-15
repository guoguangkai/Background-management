package com.yingxue.lesson.utils;

import org.springframework.stereotype.Component;

/**
 * 初始化配置代理类
 * @Component注入容器的时候，调用无参构造器，从而调用JwtTokenUtil的setTokenSettings()方法，将参数赋值为JwtTokenUtil的属性
 */
@Component
public class InitializerUtil {
    private TokenSettings tokenSettings;
    public InitializerUtil(TokenSettings tokenSettings){
        JwtTokenUtil.setTokenSettings(tokenSettings);
    }
}
