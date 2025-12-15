package com.restaurant.management.common.infrastructure.feature;

import com.restaurant.management.common.infrastructure.config.TrafficSplitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 流量切分工具类
 * 用于实现灰度发布和流量切分功能
 * 
 * 使用示例：
 * <pre>
 * {@code
 * if (trafficSplitter.shouldUseNewFeature("order-new-logic", userId)) {
 *     // 新逻辑
 *     return newOrderLogic();
 * } else {
 *     // 旧逻辑
 *     return oldOrderLogic();
 * }
 * }
 * </pre>
 * 
 * 或者使用函数式接口：
 * <pre>
 * {@code
 * return trafficSplitter.split(
 *     "order-new-logic",
 *     userId,
 *     () -> newOrderLogic(),  // 新逻辑
 *     () -> oldOrderLogic()   // 旧逻辑
 * );
 * }
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficSplitter {
    
    private final TrafficSplitProperties trafficSplitProperties;
    
    /**
     * 判断是否应该使用新功能
     * 
     * @param featureName 功能名称（配置中的 key）
     * @param identifier 用于流量切分的标识符（如 userId、orderId 等），用于保证同一用户/订单的一致性
     * @return true 表示应该使用新功能，false 表示使用旧功能
     */
    public boolean shouldUseNewFeature(String featureName, Object identifier) {
        TrafficSplitProperties.FeatureConfig config = 
                trafficSplitProperties.getFeatureConfig(featureName);
        
        // 如果功能未启用，直接返回 false
        if (!config.isEnabled()) {
            log.debug("功能 {} 未启用，使用旧逻辑", featureName);
            return false;
        }
        
        // 如果比例为 0，返回 false
        if (config.getPercentage() <= 0) {
            log.debug("功能 {} 流量比例为 0，使用旧逻辑", featureName);
            return false;
        }
        
        // 如果比例为 100，直接返回 true
        if (config.getPercentage() >= 100) {
            log.debug("功能 {} 流量比例为 100%，使用新逻辑", featureName);
            return true;
        }
        
        // 根据 identifier 的 hashCode 计算是否命中新功能
        // 使用 hashCode 可以保证同一 identifier 总是得到相同的结果
        int hash = Math.abs(Objects.hashCode(identifier));
        int bucket = hash % 100;
        boolean shouldUse = bucket < config.getPercentage();
        
        log.debug("功能 {} 流量切分: identifier={}, hash={}, bucket={}, percentage={}, shouldUse={}", 
                featureName, identifier, hash, bucket, config.getPercentage(), shouldUse);
        
        return shouldUse;
    }
    
    /**
     * 流量切分（函数式接口版本）
     * 根据配置自动选择执行新逻辑还是旧逻辑
     * 
     * @param featureName 功能名称
     * @param identifier 用于流量切分的标识符
     * @param newFeatureSupplier 新功能的执行逻辑
     * @param oldFeatureSupplier 旧功能的执行逻辑
     * @param <T> 返回值类型
     * @return 新功能或旧功能的执行结果
     */
    public <T> T split(String featureName, Object identifier,
                      Supplier<T> newFeatureSupplier,
                      Supplier<T> oldFeatureSupplier) {
        if (shouldUseNewFeature(featureName, identifier)) {
            log.info("功能 {} 使用新逻辑: identifier={}", featureName, identifier);
            return newFeatureSupplier.get();
        } else {
            log.debug("功能 {} 使用旧逻辑: identifier={}", featureName, identifier);
            return oldFeatureSupplier.get();
        }
    }
    
    /**
     * 流量切分（无返回值版本）
     * 
     * @param featureName 功能名称
     * @param identifier 用于流量切分的标识符
     * @param newFeatureRunnable 新功能的执行逻辑
     * @param oldFeatureRunnable 旧功能的执行逻辑
     */
    public void split(String featureName, Object identifier,
                     Runnable newFeatureRunnable,
                     Runnable oldFeatureRunnable) {
        if (shouldUseNewFeature(featureName, identifier)) {
            log.info("功能 {} 使用新逻辑: identifier={}", featureName, identifier);
            newFeatureRunnable.run();
        } else {
            log.debug("功能 {} 使用旧逻辑: identifier={}", featureName, identifier);
            oldFeatureRunnable.run();
        }
    }
    
    /**
     * 获取功能的当前配置信息（用于监控和调试）
     * 
     * @param featureName 功能名称
     * @return 功能配置信息
     */
    public TrafficSplitProperties.FeatureConfig getFeatureConfig(String featureName) {
        return trafficSplitProperties.getFeatureConfig(featureName);
    }
}

