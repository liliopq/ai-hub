package com.ai_hub.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 定义通知消息队列的 Exchange、Queue、Binding 及消息序列化器
 *
 * 消息路由设计：
 * - Exchange：notification.exchange（Topic 类型）
 * - Queue：notification.queue（持久化队列）
 * - Routing Key：notification.# （匹配所有通知类型）
 *
 * 通知类型路由键：
 * - notification.like       → 帖子点赞通知
 * - notification.comment    → 评论通知
 * - notification.comment_like → 评论点赞通知
 * - notification.collect    → 收藏通知
 * - notification.follow     → 关注通知
 */
@Configuration
public class RabbitMQConfig {

    /** 通知交换机名称 */
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    /** 通知队列名称 */
    public static final String NOTIFICATION_QUEUE = "notification.queue";

    /** 通知路由键前缀（Topic 模式，# 匹配所有子路由） */
    public static final String NOTIFICATION_ROUTING_KEY_PREFIX = "notification.";

    /** 通配路由键（匹配所有 notification.* 的消息） */
    public static final String NOTIFICATION_ROUTING_KEY_ALL = "notification.#";

    /**
     * 声明通知交换机（Topic 类型）
     * Topic Exchange 支持通配符路由：
     * - * 匹配一个单词
     * - # 匹配零个或多个单词
     * 设置 durable=true，服务重启后交换机仍然存在
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    /**
     * 声明通知队列
     * durable=true：队列持久化，RabbitMQ 重启后队列仍然存在
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                // 死信队列配置：处理消费失败的消息
                .withArgument("x-dead-letter-exchange", "notification.dead.exchange")
                .withArgument("x-dead-letter-routing-key", "notification.dead")
                .build();
    }

    /**
     * 死信交换机（用于存放消费失败的消息，便于排查问题）
     */
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange("notification.dead.exchange", true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("notification.dead.queue").build();
    }

    /**
     * 死信队列绑定到死信交换机
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("notification.dead");
    }

    /**
     * 将通知队列绑定到通知交换机
     * 路由键为 notification.# ，匹配所有通知类型的消息
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY_ALL);
    }

    /**
     * Jackson JSON 消息序列化器
     * 将 Java 对象序列化为 JSON 发送，接收时自动反序列化
     * 替代 RabbitMQ 默认的 JDK 序列化（可读性差、体积大）
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate 使用 JSON 序列化
     * 发送消息时自动将 NotificationTask 对象转为 JSON
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
