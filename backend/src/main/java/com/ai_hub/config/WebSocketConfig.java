package com.ai_hub.config;

import com.ai_hub.utils.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理，用于向客户端发送消息
        // /user 前缀用于点对点消息
        // /topic 前缀用于广播消息
        registry.enableSimpleBroker("/user", "/topic");
        
        // 设置应用目标前缀，客户端发送消息时使用
        registry.setApplicationDestinationPrefixes("/app");
        
        // 设置点对点消息的用户前缀
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册 STOMP 端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 WebSocket 端点，允许跨域
        registry.addEndpoint("/ws/notification")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        log.info("WebSocket 端点已注册: /ws/notification");
    }

    /**
     * 配置客户端入站通道拦截器（用于认证）
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 从 header 或 URL 参数中获取 token
                    String token = accessor.getFirstNativeHeader("token");
                    
                    // 如果 header 中没有，尝试从 URL 参数获取
                    if (token == null || token.isEmpty()) {
                        String simpSessionId = accessor.getSessionId();
                        // 这里简化处理，实际应该从 session 属性中获取
                        log.debug("WebSocket 连接请求，sessionId: {}", simpSessionId);
                    }
                    
                    // 验证 token
                    if (token != null && !token.isEmpty()) {
                        try {
                            // 移除 "Bearer " 前缀（如果有）
                            if (token.startsWith("Bearer ")) {
                                token = token.substring(7);
                            }
                            
                            // 验证 token 并获取用户ID
                            Long userId = jwtTokenProvider.getUserIdFromToken(token);
                            
                            // 将用户ID设置到会话中
                            accessor.setUser(() -> String.valueOf(userId));
                            
                            log.info("WebSocket 连接认证成功，用户ID: {}", userId);
                        } catch (Exception e) {
                            log.error("WebSocket 连接认证失败: {}", e.getMessage());
                            throw new IllegalArgumentException("Invalid token");
                        }
                    } else {
                        log.warn("WebSocket 连接缺少 token");
                        throw new IllegalArgumentException("Token is required");
                    }
                }
                
                return message;
            }
        });
    }
}
