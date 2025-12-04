package com.restaurant.management.common.infrastructure.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 配置连接、交换机、队列、消息转换器等
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;

    @Value("${spring.rabbitmq.virtual-host:/}")
    private String virtualHost;

    // ==================== 交换机名称 ====================
    /**
     * 商品相关交换机
     */
    public static final String EXCHANGE_PRODUCT = "product.exchange";

    /**
     * 订单相关交换机
     */
    public static final String EXCHANGE_ORDER = "order.exchange";

    /**
     * 库存相关交换机
     */
    public static final String EXCHANGE_INVENTORY = "inventory.exchange";

    // ==================== 队列名称 ====================
    /**
     * 商品创建队列
     */
    public static final String QUEUE_PRODUCT_CREATED = "product.created.queue";

    /**
     * 商品更新队列
     */
    public static final String QUEUE_PRODUCT_UPDATED = "product.updated.queue";

    /**
     * 订单创建队列
     */
    public static final String QUEUE_ORDER_CREATED = "order.created.queue";

    /**
     * 订单支付队列
     */
    public static final String QUEUE_ORDER_PAID = "order.paid.queue";

    /**
     * 库存预留队列
     */
    public static final String QUEUE_INVENTORY_RESERVED = "inventory.reserved.queue";

    /**
     * 库存扣减队列
     */
    public static final String QUEUE_INVENTORY_DEDUCTED = "inventory.deducted.queue";

    // ==================== 路由键 ====================
    /**
     * 商品创建路由键
     */
    public static final String ROUTING_KEY_PRODUCT_CREATED = "product.created";

    /**
     * 商品更新路由键
     */
    public static final String ROUTING_KEY_PRODUCT_UPDATED = "product.updated";

    /**
     * 订单创建路由键
     */
    public static final String ROUTING_KEY_ORDER_CREATED = "order.created";

    /**
     * 订单支付路由键
     */
    public static final String ROUTING_KEY_ORDER_PAID = "order.paid";

    /**
     * 库存预留路由键
     */
    public static final String ROUTING_KEY_INVENTORY_RESERVED = "inventory.reserved";

    /**
     * 库存扣减路由键
     */
    public static final String ROUTING_KEY_INVENTORY_DEDUCTED = "inventory.deducted";

    /**
     * 连接工厂
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        // 开启发布确认
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        // 开启发布返回
        factory.setPublisherReturns(true);
        return factory;
    }

    /**
     * RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        // 设置消息转换器
        template.setMessageConverter(messageConverter());
        // 开启强制回调
        template.setMandatory(true);
        // 设置确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息发送成功，correlationData: {}", correlationData);
            } else {
                log.error("消息发送失败，correlationData: {}, cause: {}", correlationData, cause);
            }
        });
        // 设置返回回调
        template.setReturnsCallback(returned -> {
            log.error("消息返回，exchange: {}, routingKey: {}, replyCode: {}, replyText: {}, message: {}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode(),
                    returned.getReplyText(), returned.getMessage());
        });
        return template;
    }

    /**
     * 消息转换器（JSON）
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // 设置消息转换器
        factory.setMessageConverter(messageConverter());
        // 设置并发消费者数量
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(10);
        // 设置预取数量
        factory.setPrefetchCount(10);
        // 开启手动确认
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    // ==================== 交换机定义 ====================

    /**
     * 商品交换机（Topic 类型）
     */
    @Bean
    public TopicExchange productExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_PRODUCT)
                .durable(true)  // 持久化
                .build();
    }

    /**
     * 订单交换机（Topic 类型）
     */
    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_ORDER)
                .durable(true)
                .build();
    }

    /**
     * 库存交换机（Topic 类型）
     */
    @Bean
    public TopicExchange inventoryExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_INVENTORY)
                .durable(true)
                .build();
    }

    // ==================== 队列定义 ====================

    /**
     * 商品创建队列
     */
    @Bean
    public Queue productCreatedQueue() {
        return QueueBuilder.durable(QUEUE_PRODUCT_CREATED)
                .build();
    }

    /**
     * 商品更新队列
     */
    @Bean
    public Queue productUpdatedQueue() {
        return QueueBuilder.durable(QUEUE_PRODUCT_UPDATED)
                .build();
    }

    /**
     * 订单创建队列
     */
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_CREATED)
                .build();
    }

    /**
     * 订单支付队列
     */
    @Bean
    public Queue orderPaidQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_PAID)
                .build();
    }

    /**
     * 库存预留队列
     */
    @Bean
    public Queue inventoryReservedQueue() {
        return QueueBuilder.durable(QUEUE_INVENTORY_RESERVED)
                .build();
    }

    /**
     * 库存扣减队列
     */
    @Bean
    public Queue inventoryDeductedQueue() {
        return QueueBuilder.durable(QUEUE_INVENTORY_DEDUCTED)
                .build();
    }

    // ==================== 绑定关系 ====================

    /**
     * 商品创建队列绑定
     */
    @Bean
    public Binding productCreatedBinding() {
        return BindingBuilder.bind(productCreatedQueue())
                .to(productExchange())
                .with(ROUTING_KEY_PRODUCT_CREATED);
    }

    /**
     * 商品更新队列绑定
     */
    @Bean
    public Binding productUpdatedBinding() {
        return BindingBuilder.bind(productUpdatedQueue())
                .to(productExchange())
                .with(ROUTING_KEY_PRODUCT_UPDATED);
    }

    /**
     * 订单创建队列绑定
     */
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ROUTING_KEY_ORDER_CREATED);
    }

    /**
     * 订单支付队列绑定
     */
    @Bean
    public Binding orderPaidBinding() {
        return BindingBuilder.bind(orderPaidQueue())
                .to(orderExchange())
                .with(ROUTING_KEY_ORDER_PAID);
    }

    /**
     * 库存预留队列绑定
     */
    @Bean
    public Binding inventoryReservedBinding() {
        return BindingBuilder.bind(inventoryReservedQueue())
                .to(inventoryExchange())
                .with(ROUTING_KEY_INVENTORY_RESERVED);
    }

    /**
     * 库存扣减队列绑定
     */
    @Bean
    public Binding inventoryDeductedBinding() {
        return BindingBuilder.bind(inventoryDeductedQueue())
                .to(inventoryExchange())
                .with(ROUTING_KEY_INVENTORY_DEDUCTED);
    }
}

