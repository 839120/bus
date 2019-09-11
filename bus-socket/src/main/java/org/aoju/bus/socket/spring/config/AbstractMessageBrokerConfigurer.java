package org.aoju.bus.socket.spring.config;

import org.aoju.bus.socket.spring.extension.StompEndpointRegistry;

/**
 * 增加消息拦截器
 *
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
public abstract class AbstractMessageBrokerConfigurer {

    public void registerStompEndpoints(StompEndpointRegistry registry) {

    }

}