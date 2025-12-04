package com.restaurant.management.common.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存配置（Caffeine）
 * 支持创建多个不同策略的缓存实例
 * 如果某个场景不需要本地缓存，可以不注入对应的 Cache
 */
@Configuration
public class CacheConfig {

    @Value("${cache.local.max-size:1000}")
    private long maxSize;

    @Value("${cache.local.expire-after-write-seconds:300}")
    private long expireAfterWriteSeconds;

    @Value("${cache.local.expire-after-access-seconds:180}")
    private long expireAfterAccessSeconds;

    /**
     * 默认本地缓存（商品相关）
     * 用于高频访问的数据，如商品信息、库存信息
     * 过期时间：写入后 300 秒，访问后 180 秒
     */
    @Bean("defaultLocalCache")
    public Cache<String, Object> defaultLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireAfterWriteSeconds, TimeUnit.SECONDS)
                .expireAfterAccess(expireAfterAccessSeconds, TimeUnit.SECONDS)
                .recordStats() // 开启统计功能
                .build();
    }

    /**
     * 长期缓存（长时间缓存）
     * 过期时间：写入后 1 小时
     * 适用于：不经常变化的数据
     */
    @Bean("longTermCache")
    public Cache<String, Object> longTermCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(2000)
                .recordStats()
                .build();
    }
}

