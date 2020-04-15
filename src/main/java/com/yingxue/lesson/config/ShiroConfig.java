package com.yingxue.lesson.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.yingxue.lesson.shiro.CustomAccessControlerFilter;
import com.yingxue.lesson.shiro.CustomHashedCredentialsMatcher;
import com.yingxue.lesson.shiro.CustomRealm;
import com.yingxue.lesson.shiro.RedisCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置shiro
 */
@Configuration
public class ShiroConfig {

    /**
     * 将缓存管理器注入到容器
     * shiro默认整合了EhCache来实现缓存，现在换为redis
     */
    @Bean
    public RedisCacheManager redisCacheManager(){
        return new RedisCacheManager();
    }

    /**
     * 注入自定义校验
     * Shiro 提供了用于加密密码和验证密码服务的 CredentialsMatcher 接口，而 HashedCredentialsMatcher 正是 CredentialsMatcher 的一个实现类。
     * 现在我们继承HashedCredentialsMatcher，实现自定义CredentialsMatcher
     * Credentia：提供证明书(或证件);
     */
    @Bean
    public CustomHashedCredentialsMatcher customHashedCredentialsMatcher(){
        return new CustomHashedCredentialsMatcher();
    }

    /**
     * 注入自定义域
     * CustomRealm（自定义Realm）是通过继承AuthrizingRealm
     * 重写doGetAuthenticationInfo(AuthenticationToken token)【认证】   --- 正常逻辑去数据取数据来比对
     * 和 doGetAuthorizationInfo(PrincipalCollection principalCollection)【授权】  --- 正常逻辑去数据取数据来授权
     *
     *      * Realm：域，Realm 充当了 Shiro 与应用安全数据间的“桥梁”或者“连接器”。也就是说，当对用户执行认证（登录）和授权（访问控制）验证时，
     *      * Shiro 会从应用配置的 Realm 中查找用户及其权限信息。从这个意义上讲，Realm 实质上是一个安全相关的 DAO：它封装了数据源的连接细节，
     *      * 并在需要时将相关数据提供给 Shiro 。当配置 Shiro时，你必须至少指定一个 Realm ，用于认证和（或）授权。
     */
    @Bean
    public CustomRealm customRealm(){
        CustomRealm customRealm=new CustomRealm();
        //换成自定义的密码匹配器
        customRealm.setCredentialsMatcher(customHashedCredentialsMatcher());
        //在域中加入缓存，这个缓存是我们自己实现的redis。看源码可以知道域中是每次都会先去缓存中查，没有就会交由认证器，
        //然后调用RedisCache的put将键值对加入缓存，下次缓存拿，就不会进入我们自定义的认证器了。缓存没有才去我们自定义的域中读取
        customRealm.setCacheManager(redisCacheManager());
        return customRealm;
    }

    /**
     * 注入安全管理器
     *SecurityManager是Shiro框架的核心，Shiro通过SecurityManager来管理内部组件实例，并通过它来提供安全管理的各种服务。
     * SecurityManager继承了接口Authorizer(认证器),SessionManager(会话管理器),Authenticator(授权器)
     */
    @Bean
    public SecurityManager securityManager(){
        //设置shiro默认的web安全管理器
        DefaultWebSecurityManager defaultWebSecurityManager=new DefaultWebSecurityManager();
        //将realm加入到安全管理器里
        defaultWebSecurityManager.setRealm(customRealm());
        return defaultWebSecurityManager;
    }
    /**
     * 注入 shiro 过滤器
     * 这里主要是配置一些要放行的 url和要拦截认证的 url
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        /**
         * 自定义过滤器限制并发人数
         */
        //新建集合存储过滤器，形成过滤器链
        LinkedHashMap<String, Filter> filtersMap = new LinkedHashMap<>();
        //键为过滤器的名称，值为过滤器的实现对象。
        filtersMap.put("token", new CustomAccessControlerFilter());
        shiroFilterFactoryBean.setFilters(filtersMap);
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 配置不会被拦截的链接 顺序判断
        filterChainDefinitionMap.put("/api/user/login", "anon");
        filterChainDefinitionMap.put("/index/**","anon");
        filterChainDefinitionMap.put("/images/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/layui/**", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/treetable-lay/**", "anon");
        filterChainDefinitionMap.put("/api/user/token", "anon");
        //放开swagger-ui地址
        filterChainDefinitionMap.put("/swagger/**", "anon");
        filterChainDefinitionMap.put("/v2/api-docs", "anon");
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/druid/**", "anon");
        filterChainDefinitionMap.put("/favicon.ico", "anon");
        filterChainDefinitionMap.put("/captcha.jpg", "anon");
        filterChainDefinitionMap.put("/","anon");
        filterChainDefinitionMap.put("/csrf","anon");
        filterChainDefinitionMap.put("/**","token,authc");
        //配置shiro默认登录界面地址，前后端分离中登录界面跳转应由前端路由控制，后台仅返回json数据
        shiroFilterFactoryBean.setLoginUrl("/api/user/unLogin");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    /**
     * 开启shiro aop注解支持.
     * 使用代理方式;所以需要开启代码支持;
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
    @Bean
    @ConditionalOnMissingBean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }

}
