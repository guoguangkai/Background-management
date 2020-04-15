package com.yingxue.lesson.shiro;

import com.alibaba.fastjson.JSON;
import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.utils.DataResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 访问控制过滤器，适用于简单的验证。
 *      主要是拦截需要认证的请求，首先验证客户端 header 是否携带了 token ，如果没有携带直接响应客户端，引导客户端到登录界面进行登录操作
 *      如果客户端 header 已经携带有 token ，放开进入 shiro SecuritayManager 验证
 *
 *  以前shiro验证: UsernamePasswordToken token = new UsernamePasswordToken(username, password);用用户名密码构造一个token
 *                SecurityUtils.getSubject().login(token); 构造的token提交到安全管理器，然后流转到用户认证器进行用户认证
 *                最后shiro会从应用配置的 Realm中查找用户及其权限信息（拿到DB里存的用户密码），和生成的UsernamePasswordToken进行比对
 *      现在我们使用jwt作为凭证，不能用shiro以前的认证方式，现在是基于token的认证，要重写shiro的过滤器
 *      首先要验证token是否携带，是否有效等等，然后通过token获取主体，最后交给shiro取认证
 */
@Slf4j
public class CustomAccessControlerFilter extends AccessControlFilter {
    /**
     * 是否允许进入下一个链式调用
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) {
        return false;
    }

    /**
     * 当不允许通过的时候。
     * @return true：表示自己不处理，进入下一个链式调用
     *         false：表示自己已经处理了（比如重定向到另一个页面）
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) {
        //拿到请求
        HttpServletRequest request= (HttpServletRequest) servletRequest;
        log.info(request.getMethod());
        log.info(request.getRequestURL().toString());
        //判断客户端是否携带accessToken
        try {
            String accessToken=request.getHeader(Constant.ACCESS_TOKEN);
            if(StringUtils.isEmpty(accessToken)){
                //如果没有携带accessToken，抛出异常，响应给客户端。但这里抛出的异常，在全局异常中监控不到，所以我们要自己try-catch处理
                throw new BusinessException(BaseResponseCode.TOKEN_NOT_NULL);
            }
            // UsernamePasswordToken token = new UsernamePasswordToken(username, password)；现在改为token
            CustomUsernamePasswordToken customUsernamePasswordToken=new CustomUsernamePasswordToken(accessToken);
            // 调用login()后，委托给shiro的Realm安全管理器 进行登录验证处理。【我们继承了HashedCredentialsMatcher，重写了shiro原来自带的UsernamePasswordToken比对方法，改造为比对传入的jwt】
            this.getSubject(servletRequest,servletResponse).login(customUsernamePasswordToken);
        } catch (BusinessException e) {
            //捕获异常后的异常处理方式
            customRsponse(e.getCode(),e.getDefaultMessage(),servletResponse);
            return false;
        } catch (AuthenticationException e) {
            // getCause()返回此异常的原因
            if(e.getCause() instanceof BusinessException){
                //主动抛出的异常
                BusinessException exception= (BusinessException) e.getCause();
                customRsponse(exception.getCode(),exception.getDefaultMessage(),servletResponse);
            }else {
                //系统抛出的异常
                customRsponse(BaseResponseCode.SHIRO_AUTHENTICATION_ERROR.getCode(),BaseResponseCode.SHIRO_AUTHENTICATION_ERROR.getMsg(),servletResponse);
            }
           return false;
        }
        return true;
    }

    /**
     * 自定义错误响应
     */
    private void customRsponse(int code, String msg, ServletResponse response){
        //  用DataResult封装异常信息，然后直接写个流OutputStream，返回给客户端相应的JSON格式的信息
        try {
            DataResult result=DataResult.getResult(code,msg);
            response.setContentType("application/json; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //将DataResult转为json
            String userJson = JSON.toJSONString(result);
            //Servlet取得输出流对象
            OutputStream out = response.getOutputStream();
            //json 转为byte数组,并写入输出流对象
            out.write(userJson.getBytes("UTF-8"));
            //清空缓冲区的数据流
            out.flush();
        } catch (IOException e) {
            log.error("eror={}",e);
        }
    }
}
