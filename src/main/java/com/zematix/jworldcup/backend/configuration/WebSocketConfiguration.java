package com.zematix.jworldcup.backend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Profile("!develop")
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
	
	@Value("${rabbitMQ.user}")
	private String rabbitMQUser;

	@Value("${rabbitMQ.password}")
	private String rabbitMQPassword;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
//				.setAllowedOrigins("*")
//				.setAllowedOrigins("http://localhost:4200")
				.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app");

		// Enables a full featured broker (RabbitMQ here)
        registry.enableStompBrokerRelay("/topic", "/queue")
                /*.setRelayHost("localhost")
                .setRelayPort(61613)*/
                .setClientLogin(rabbitMQUser)
                .setClientPasscode(rabbitMQPassword);
	}
}
