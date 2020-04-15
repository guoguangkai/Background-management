package com.yingxue.lesson.shiro;

import com.alibaba.fastjson.JSON;
import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.service.RedisService;
import com.yingxue.lesson.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 每次认证/授权 都会去执行CustomRealm 里的doGetAuthorizationInfo和doGetAuthenticationInfo方法，执行解析token，各种验证。因为角色权限这些信息不经常更换，每次都反复验证，耗资源
 *RedisCache<K, V> 实现shiro Cache<K, V>缓存接口，并重写Cache<K, V> get、put、remove、clear、size、keys、values等方法，
 * 这些方法都是 shiro 在对缓存的一些操作，就是当 shiro 操作缓存的时候都会调用相应的方法，我们只需重写这些相应的方法就可以把 shiro 的缓存信息存入到 redis了。
 * 这就是一个优秀的开源框架所具备的扩展性，它提供了一个cacheManager 缓存管理器我们只需重新这个管理器即可。
 */
@Slf4j
public class RedisCache<K,V> implements Cache<K,V> {
    private String cacheKey;
    //设置过期时间，这里设置一天，一天之后自动清除，如果用户再提交主体，就又再存到缓存里。不设置也可以，就是永久，用户退出清除它的缓存就可以了。
    private long expire = 24;
    /**
     * 因为RedisCache没有交由spring容器管理，所以RedisService @Autowired注入不进来
     * 需要用第三方代理类初始化它才可以
     */
    private RedisService redisService;
    public RedisCache(RedisService redisService){
        //初始化key
        this.cacheKey= Constant.IDENTIFY_CACHE_KEY;
        //初始化RedisService
        this.redisService=redisService;
    }
    /**
     * 获取
     * 传入的key就是我们的jwt
     */
    @Override
    public V get(K key) throws CacheException {
        log.info("Shiro从缓存中获取数据KEY值[{}]",key);
        if (key == null) {
            return null;
        }
        try {
            String redisCacheKey = getRedisCacheKey(key);
            Object rawValue = redisService.get(redisCacheKey);
            if (rawValue == null) {
                return null;
            }
            SimpleAuthorizationInfo simpleAuthenticationInfo= JSON.parseObject(rawValue.toString(),SimpleAuthorizationInfo.class);
            V value = (V) simpleAuthenticationInfo;
            return value;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    /**
     * 存储
     */
    @Override
    public V put(K key, V value) throws CacheException {
        log.info("put key [{}]",key);
        if (key == null) {
            log.warn("Saving a null key is meaningless, return value directly without call Redis.");
            return value;
        }
        try {
            /**
             * token可以反复签发，一个用户可以签发多个token，所以token不能做唯一标识，唯一的只有用户ID
             * 所以我们自定义一个方法，解析用户ID，再和其它常量组合成唯一标识
             */
            String redisCacheKey = getRedisCacheKey(key);
            redisService.set(redisCacheKey, value != null ? value : null, expire, TimeUnit.HOURS);
            return value;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }
    /**
     * 通过key 删除redis中的标记
     */
    @Override
    public V remove(K key) throws CacheException {
        log.info("remove key [{}]",key);
        if (key == null) {
            return null;
        }
        try {
            String redisCacheKey = getRedisCacheKey(key);
            Object rawValue = redisService.get(redisCacheKey);
            V previous = (V) rawValue;
            redisService.delete(redisCacheKey);
            return previous;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }
    /**
     * 清除所有标记
     */
    @Override
    public void clear() throws CacheException {
        log.debug("clear cache");
        Set<String> keys = null;
        try {
            keys = redisService.keys(this.cacheKey + "*");
        } catch (Exception e) {
            log.error("get keys error", e);
        }
        if (keys == null || keys.size() == 0) {
            return;
        }
        for (String key: keys) {
            redisService.delete(key);
        }
    }
    /**
     * 查询缓存数量
     */
    @Override
    public int size() {
        int result = 0;
        try {
            result = redisService.keys(this.cacheKey + "*").size();
        } catch (Exception e) {
            log.error("get keys error", e);
        }
        return result;
    }
    /**
     * 获取redis中键的集合
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keys() {
        Set<String> keys = null;
        try {
            keys = redisService.keys(this.cacheKey + "*");
        } catch (Exception e) {
            log.error("get keys error", e);
            return Collections.emptySet();
        }
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptySet();
        }
        Set<K> convertedKeys = new HashSet<>();
        for (String key:keys) {
            try {
                convertedKeys.add((K) key);
            } catch (Exception e) {
                log.error("deserialize keys error", e);
            }
        }
        return convertedKeys;
    }
    /**
     * 获取redis中值的集合
     */
    @Override
    public Collection<V> values() {
        Set<String> keys = null;
        try {
            keys = redisService.keys(this.cacheKey + "*");
        } catch (Exception e) {
            log.error("get values error", e);
            return Collections.emptySet();
        }
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptySet();
        }
        List<V> values = new ArrayList<V>(keys.size());
        for (String key : keys) {
            V value = null;
            try {
                value = (V) redisService.get(key);
            } catch (Exception e) {
                log.error("deserialize values= error", e);
            }
            if (value != null) {
                values.add(value);
            }
        }
        return Collections.unmodifiableList(values);
    }
    /**
     * 生成redis的key
     */
    private String getRedisCacheKey(K key) {
        if(null==key){
            return null;
        }else {
            return this.cacheKey+ JwtTokenUtil.getUserId(key.toString());
        }
    }
}
