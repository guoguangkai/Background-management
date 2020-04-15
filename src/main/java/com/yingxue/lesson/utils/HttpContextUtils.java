package com.yingxue.lesson.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * http上下文
 * 想在Service方法中使用HttpServletRequest的API，但是又不想把HttpServletRequest对象当作这个Service方法的参数传过来
 * 在SpringMVC中就有开箱即用的实现。代码是：((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
 * 原理：在请求执行之前获取到HttpServletRequest，把它set()到某个类的ThreadLocal类型的静态成员中，使用的时候直接通过静态方式访问到这个ThreadLocal对象，
 * 		调用它的get()方法，即可获取到线程隔离的HttpServletRequest了。最后，在请求结束后，要调用ThreadLocal的remove()方法，清理资源引用。【线程隔离】
 */
public class HttpContextUtils {

	public static HttpServletRequest getHttpServletRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}
}
