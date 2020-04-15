package com.yingxue.lesson.shiro;

import com.yingxue.lesson.service.RedisService;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName: RedisCacheManager
 */
public class RedisCacheManager implements CacheManager {
    @Autowired
    private RedisService redisService;
    @Override
    public <K, V> Cache<K, V> getCache(String s) throws CacheException {
        //初始化RedisCache中的相关属性
        return new RedisCache<>(redisService);
    }
}
