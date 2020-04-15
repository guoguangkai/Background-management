package com.yingxue.lesson.config;

import com.yingxue.lesson.serializer.MyStringRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 *
 *  Jedis是Redis官方推荐的面向Java的操作Redis的客户端，而RedisTemplate是SpringDataRedis中对JedisApi的高度封装。
 *  SpringDataRedis相对于Jedis来说可以方便地更换Redis的Java客户端，比Jedis多了自动管理连接池的特性，方便与其他Spring框架进行搭配使用如：SpringCache
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> redisTemplate=new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //使用StringRedisSerializer做key的序列化时，StringRedisSerializer的泛型指定的是String，传其他对象就会报类型转换错误。
        StringRedisSerializer stringRedisSerializer=new StringRedisSerializer();
        //针对Value值的“序列化/反序列化”，自定义策略
        MyStringRedisSerializer myStringRedisSerializer=new MyStringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(myStringRedisSerializer);
        redisTemplate.setValueSerializer(myStringRedisSerializer);
        return redisTemplate;
    }
}
