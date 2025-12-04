package com.restaurant.management.common.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 两级缓存管理器
 * 第一级：本地缓存（Caffeine）- 快速访问（可选）
 * 第二级：Redis 缓存 - 分布式共享
 * 
 * 策略：先查本地缓存（如果存在），未命中再查 Redis，都未命中则从数据库加载
 * 
 * 设计理念：
 * 1. 本地缓存和 Redis 使用完全相同的 key
 * 2. 本地缓存是可选的，如果不需要可以设置为 null
 * 3. 支持不同场景使用不同的本地缓存策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiLevelCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // 本地缓存（可选，如果不需要本地缓存可以不注入）
    // 使用 @Autowired(required = false) 表示可选注入
    @Autowired(required = false)
    @Qualifier("defaultLocalCache")
    private Cache<String, Object> localCache;

    /**
     * 获取缓存值（两级缓存）
     * 
     * @param key 完整的缓存键（本地缓存和 Redis 都使用此 key）
     * @param valueLoader 值加载器（缓存未命中时调用）
     * @return 缓存值
     */
    public <T> T get(String key, Callable<T> valueLoader) {
        try {
            // 1. 先查本地缓存（如果存在，使用相同的 key）
            if (localCache != null) {
                Object cachedValue = localCache.getIfPresent(key);
                if (cachedValue != null) {
                    log.debug("本地缓存命中，key: {}", key);
                    return (T) cachedValue;
                }
            }

            // 2. 再查 Redis（使用相同的 key）
            T value = (T) redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Redis 缓存命中，key: {}", key);
                // 回填本地缓存
                if (localCache != null) {
                    localCache.put(key, value);
                }
                return value;
            }

            // 3. 都未命中，从数据源加载
            log.debug("缓存未命中，从数据源加载，key: {}", key);
            T loadedValue = valueLoader.call();
            
            // 4. 写入两级缓存
            if (loadedValue != null) {
                put(key, loadedValue);
            }
            
            return loadedValue;
        } catch (Exception e) {
            log.error("获取缓存失败，key: {}", key, e);
            try {
                return valueLoader.call();
            } catch (Exception ex) {
                log.error("数据源加载失败", ex);
                throw new RuntimeException("缓存和数据源加载都失败", ex);
            }
        }
    }

    /**
     * 写入缓存（两级缓存）
     * 
     * @param key 完整的缓存键（本地缓存和 Redis 都使用此 key）
     * @param value 缓存值
     */
    public void put(String key, Object value) {
        if (value == null) {
            return;
        }

        try {
            // 写入本地缓存（如果存在，使用相同的 key）
            if (localCache != null) {
                localCache.put(key, value);
            }

            // 写入 Redis（使用相同的 key）
            redisTemplate.opsForValue().set(key, value, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("写入缓存失败，key: {}", key, e);
        }
    }

    /**
     * 删除缓存（两级缓存）
     * 
     * @param key 完整的缓存键（本地缓存和 Redis 都使用此 key）
     */
    public void evict(String key) {
        try {
            // 删除本地缓存（如果存在，使用相同的 key）
            if (localCache != null) {
                localCache.invalidate(key);
            }

            // 删除 Redis（使用相同的 key）
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除缓存失败，key: {}", key, e);
        }
    }

    /**
     * 清空本地缓存
     * 注意：只清空本地缓存，不清空 Redis（避免影响其他实例）
     */
    public void clearLocalCache() {
        try {
            if (localCache != null) {
                localCache.invalidateAll();
                log.info("已清空本地缓存");
            }
        } catch (Exception e) {
            log.error("清空本地缓存失败", e);
        }
    }
}

