package com.yingxue.lesson.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 配置读取类
 * Duration字面意思是持续时间 表示秒或纳秒时间间隔，适合处理较短的时间，需要更高的精确性
 */
@Component
@Data
@ConfigurationProperties(prefix = "jwt")
public class TokenSettings {
    private String secretKey;
    private Duration accessTokenExpireTime;
    private Duration refreshTokenExpireTime;
    private Duration refreshTokenExpireAppTime;
    private String issuer;
}
