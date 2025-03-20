package com.communicator.config.websockets;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//import com.communicator.services.utils.UserHandshakeInterceptor;
/**
 * Configures the WebSocket message broker for the application.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

//    @Autowired
//    private UserHandshakeInterceptor handshakeInterceptor;

    /**
     * Registers the STOMP endpoints used by WebSocket clients.
     *
     * @param registry the registry to which endpoints are added.
     */
    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Configures the message broker for routing messages.
     *
     * @param registry the registry for configuring the message broker.
     */
    @Override
    public void configureMessageBroker(final MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        registry.setUserDestinationPrefix("/user");  // Add this line
    }
}
