package com.yingxue.lesson.aop.aspect;

import com.alibaba.fastjson.JSON;
import com.yingxue.lesson.aop.annotation.MyLog;
import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.entity.SysLog;
import com.yingxue.lesson.mapper.SysLogMapper;
import com.yingxue.lesson.utils.HttpContextUtils;
import com.yingxue.lesson.utils.IPUtils;
import com.yingxue.lesson.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

/**
 * 1.目标类（target）：需要被增强的类。
 * 2.连接点（Joinpoint）：可能被增强的点，目标类中的所有方法。
 * 3.切入点（Pointcut）：将会被增强的连接点，目标类中被增强的方法。
 * 4.通知/增强（Advice）：对切入点增强的内容。增强的内容通常以方法的形式体现的。增强执行的位置不同，称呼不同。
 * （前置通知、后置通知、环绕通知、抛出异常通知、最终通知）
 * 通知方法所在的类，通常称为切面类。
 * 5.切面（Aspect）：通知和切入点的结合。一个通知对应一个切入点就形成一条线，多个通知对应多个切入点形成多条线，多条线形成了一个面，我们称为切面。
 * 6.织入（Weaving）： 生成切面并创建代理对象的过程。（将通知和切入点的结合，并创建代理对象的过程）
 *                      织入是将增强添加到目标类具体连接点上的过程，AOP有三种织入方式：
 *                          ①编译期织入：需要特殊的Java编译期（例如AspectJ的ajc）；
 *                          ②装载期织入：要求使用特殊的类加载器，在装载类的时候对类进行增强；
 *                          ③运行时织入：在运行时为目标类生成代理实现增强。Spring采用了动态代理的方式实现了运行时织入，而AspectJ采用了编译期织入和装载期织入的方式。
 */

/**
 * 切面类
 * Spring AOP面向切面编程，可以用来配置事务、做日志、权限验证、在用户请求时做一些处理等等。用@Aspect做一个切面，就可以直接实现。
 */
@Aspect//切面: 通常是一个类，里面可以定义切入点和通知
@Component
@Slf4j
public class SysLogAspect {
    @Autowired
    private SysLogMapper sysLogMapper;
    /**
     * 配置切入点(以@MyLog注解为标志),只要出现 @MyLog注解都会进入
     * @annotation：用于匹配当前执行方法持有指定注解的方法；
     */
    @Pointcut("@annotation(com.yingxue.lesson.aop.annotation.MyLog)")
    public void logPointCut(){

    }

    /**
     * 环绕增强
     * JoinPoint类，用来获取代理类和被代理类的信息。ProceedingJoinPoint继承JoinPoint类
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //执行目标方法
        Object result = point.proceed();//ProceedingJoinPoint 执行proceed方法的作用是让目标方法执行，这也是环绕通知和前置、后置通知方法的一个最大区别。
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        //保存日志
        try {
            saveSysLog(point, time);
        } catch (Exception e) {
            log.error("e={}",e);
        }
        return result;
    }
    /**
     * 把日志保存
     */
    private void saveSysLog(ProceedingJoinPoint joinPoint, long time) {
        //获取被增强的方法相关信息(signature是信号,标识的意思)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //MethodSignature封装了两个方法,一个获取方法的返回值类型,一个是获取封装的Method对象,
        Method method = signature.getMethod();
        SysLog sysLog = new SysLog();
        //通过Method对象可以获取方法上的注解，这里只获取MyLog注解
        MyLog myLog = method.getAnnotation(MyLog.class);
        if(myLog != null){
            //把注解上的描述赋值到sysLog对象
            sysLog.setOperation(myLog.title()+"-"+myLog.action());
        }
        //被代理类的对象，全路径名 eg:com.yingxue.lesson.aop.aspect.SysLogAspect
        String className = joinPoint.getTarget().getClass().getName();
        // 調用的方法名 eg:saveSysLog()
        String methodName = signature.getName();
        //eg:com.yingxue.lesson.aop.aspect.SysLogAspect.saveSysLog()
        sysLog.setMethod(className + "." + methodName + "()");
        //打印该方法耗时时间
        log.info("请求{}.{}耗时{}毫秒",className,methodName,time);
        try {
            //获取请求的参数
            Object[] args = joinPoint.getArgs();
            String params=null;
            if(args.length!=0){
                params= JSON.toJSONString(args);
            }
            sysLog.setParams(params);
        } catch (Exception e) {

        }
        //获取request
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        //获取IP地址并赋值给sysLog对象
        sysLog.setIp(IPUtils.getIpAddr(request));
        log.info("Ip{}，接口地址{}，请求方式{}，入参：{}",sysLog.getIp(),request.getRequestURL(),request.getMethod(),sysLog.getParams());
        //从请求头获取token
        String token = request.getHeader(Constant.ACCESS_TOKEN);
        //从token获取用户id
        String userId= JwtTokenUtil.getUserId(token);
        String username= JwtTokenUtil.getUserName(token);
        sysLog.setUsername(username);
        sysLog.setUserId(userId);
        //耗时
        sysLog.setTime((int) time);
        sysLog.setId(UUID.randomUUID().toString());
        sysLog.setCreateTime(new Date());
        log.info(sysLog.toString());
        //插入数据库
        sysLogMapper.insertSelective(sysLog);

    }
}
