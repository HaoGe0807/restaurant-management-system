package com.restaurant.management.common.infrastructure.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Nacos 配置中心配置
 * 支持动态配置刷新和监听
 * 
 * 注意：这是一个简化版本，实际生产环境建议使用 Spring Cloud Alibaba
 * 或者实现更完善的配置刷新机制
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "nacos.config", name = "enabled", havingValue = "true", matchIfMissing = false)
public class NacosConfig {

    @Value("${nacos.config.server-addr:localhost:8848}")
    private String serverAddr;

    @Value("${nacos.config.namespace:}")
    private String namespace;

    @Value("${nacos.config.group:DEFAULT_GROUP}")
    private String group;

    @Value("${nacos.config.data-id:${spring.application.name}.yml}")
    private String dataId;

    private final ApplicationContext applicationContext;

    public NacosConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 创建 Nacos ConfigService
     * 支持动态配置刷新
     */
    @Bean
    public ConfigService configService() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        if (namespace != null && !namespace.isEmpty()) {
            properties.put("namespace", namespace);
        }
        
        ConfigService configService = com.alibaba.nacos.api.config.ConfigFactory.createConfigService(properties);
        
        // 添加配置监听器，实现动态刷新
        configService.addListener(dataId, group, new Listener() {
            @Override
            public Executor getExecutor() {
                return null; // 使用默认执行器
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info("Nacos配置已更新: dataId={}, group={}", dataId, group);
                log.debug("新配置内容: {}", configInfo);
                
                // 解析 YAML 配置
                try {
                    Yaml yaml = new Yaml();
                    Map<String, Object> configMap = yaml.load(configInfo);
                    Map<String, Object> flatMap = flattenMap(configMap, "");
                    
                    // 发布配置刷新事件（自定义事件，用于通知配置变更）
                    applicationContext.publishEvent(new ConfigRefreshEvent(flatMap));
                    log.info("配置刷新事件已发布，共 {} 个配置项", flatMap.size());
                } catch (Exception e) {
                    log.error("解析Nacos配置失败", e);
                }
            }
        });
        
        log.info("Nacos ConfigService 初始化成功: serverAddr={}, namespace={}, dataId={}, group={}", 
                serverAddr, namespace, dataId, group);
        return configService;
    }
    
    /**
     * 将嵌套的 Map 扁平化
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> flattenMap(Map<String, Object> map, String prefix) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                result.putAll(flattenMap((Map<String, Object>) value, key));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }
    
    /**
     * 配置刷新事件
     */
    public static class ConfigRefreshEvent {
        private final Map<String, Object> configMap;
        
        public ConfigRefreshEvent(Map<String, Object> configMap) {
            this.configMap = configMap;
        }
        
        public Map<String, Object> getConfigMap() {
            return configMap;
        }
    }
}

