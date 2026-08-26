package com.planwith.planwith_fo_meeting.adapter.out.kafka;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class MeetingKafkaProducerConfig {

	@Bean
	DefaultKafkaProducerFactoryCustomizer meetingProducerTimeouts() {
		return factory -> factory.updateConfigs(Map.of(
				ProducerConfig.MAX_BLOCK_MS_CONFIG, "30000",
				ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "20000",
				ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "40000",
				ProducerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, "10000"
		));
	}
}
