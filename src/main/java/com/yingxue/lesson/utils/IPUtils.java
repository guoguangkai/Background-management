package com.yingxue.lesson.utils;

import com.alibaba.druid.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * IP地址
 *
 */
public class IPUtils {

	private static Logger logger = LoggerFactory.getLogger(IPUtils.class);

	/**
	 * 获取IP地址
	 * 在java中，获取ip地址的方法是：request.getRemoteAddr()
	 * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址,这时候获取到的ip地址是127.0.0.1或者是192.168.1.110
	 * 原因在于，服务器与客户端之间多了一个层次，即代理。因此服务器无法直接拿到客户端的 IP，服务器端应用也无法直接通过转发请求的地址返回给客户端。
	 * 但是在转发请求的HTTP头信息中，增加了X-FORWARDED-FOR信息。用以跟踪原有的客户端 IP地址和原来客户端请求的服务器地址。
	 * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = null;
		try {
			//X-Forwarded-For 是一个扩展头,用来表示 HTTP 请求端真实 IP
			ip = request.getHeader("x-forwarded-for");
			if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
				//这个一般是经过apache http服务器的请求才会有，用apache http做代理时一般会加上Proxy-Client-IP请求头
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (StringUtils.isEmpty(ip) || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				//而WL- Proxy-Client-IP是他的weblogic插件加上的头
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
				////有些代理服务器会加上此相应头
				ip = request.getHeader("HTTP_CLIENT_IP");
			}
			if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
		} catch (Exception e) {
			logger.error("IPUtils ERROR ", e);
		}

		// 使用代理，则获取第一个IP地址
		if (!StringUtils.isEmpty(ip) && ip.length() > 15) {
			if (ip.indexOf(",") > 0) {
				ip = ip.substring(0, ip.indexOf(","));
			}
		}

		return ip;
	}

}
