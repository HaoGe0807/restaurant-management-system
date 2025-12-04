package com.restaurant.management.common.infrastructure.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 消息发送工具类
 * 封装消息发送的通用方法
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQMessageSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到指定交换机和路由键
     * 
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息对象（会自动序列化为 JSON）
     */
    public void send(String exchange, String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.debug("消息发送成功，exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message);
        } catch (Exception e) {
            log.error("消息发送失败，exchange: {}, routingKey: {}, message: {}", exchange, routingKey, message, e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    /**
     * 发送延迟消息
     * 
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息对象
     * @param delayMillis 延迟时间（毫秒）
     */
    public void sendDelayed(String exchange, String routingKey, Object message, long delayMillis) {
        try {
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setDelay((int) delayMillis);
            Message rabbitMessage = rabbitTemplate.getMessageConverter().toMessage(message, messageProperties);
            rabbitTemplate.send(exchange, routingKey, rabbitMessage);
            log.debug("延迟消息发送成功，exchange: {}, routingKey: {}, delay: {}ms", exchange, routingKey, delayMillis);
        } catch (Exception e) {
            log.error("延迟消息发送失败，exchange: {}, routingKey: {}, delay: {}ms", exchange, routingKey, delayMillis, e);
            throw new RuntimeException("延迟消息发送失败", e);
        }
    }
}

