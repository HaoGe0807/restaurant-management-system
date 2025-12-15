package com.restaurant.management.common.infrastructure.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 流量切分配置属性
 * 支持从 Nacos 动态刷新
 * 
 * 配置示例（在 Nacos 中）:
 * traffic:
 *   split:
 *     order-new-logic:
 *       enabled: true
 *       percentage: 10  # 10% 的流量进入新逻辑
 *     product-new-feature:
 *       enabled: true
 *       percentage: 50  # 50% 的流量进入新功能
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "traffic.split")
public class TrafficSplitProperties {
    
    /**
     * 功能开关配置
     * key: 功能名称（如 "order-new-logic"）
     * value: 该功能的流量切分配置
     */
    private Map<String, FeatureConfig> features = new HashMap<>();
    
    /**
     * 单个功能的流量切分配置
     */
    @Data
    public static class FeatureConfig {
        /**
         * 是否启用该功能
         */
        private boolean enabled = false;
        
        /**
         * 流量切分比例（0-100）
         * 例如：10 表示 10% 的流量进入新功能
         */
        private int percentage = 0;
    }
    
    /**
     * 获取指定功能的配置
     * @param featureName 功能名称
     * @return 功能配置，如果不存在则返回默认配置（enabled=false, percentage=0）
     */
    public FeatureConfig getFeatureConfig(String featureName) {
        return features.getOrDefault(featureName, new FeatureConfig());
    }
    
    /**
     * 监听配置刷新事件，实现动态刷新
     * 注意：这是一个简化实现，实际生产环境建议使用 Spring Cloud 的 @RefreshScope
     */
    @EventListener
    public void onConfigRefresh(NacosConfig.ConfigRefreshEvent event) {
        Map<String, Object> configMap = event.getConfigMap();
        // 重新加载 traffic.split 配置
        // 注意：这里需要手动解析配置并更新 features
        // 实际使用中，建议使用 Spring Cloud Config 或 Apollo 等成熟的配置中心
        log.info("收到配置刷新事件，配置项数量: {}", configMap.size());
        // TODO: 实现配置重新加载逻辑
    }
}

