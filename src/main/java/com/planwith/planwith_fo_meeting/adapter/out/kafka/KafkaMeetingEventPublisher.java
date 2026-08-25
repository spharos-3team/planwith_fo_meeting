package com.planwith.planwith_fo_meeting.adapter.out.kafka;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_meeting.adapter.out.kafka.dto.EventEnvelope;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaMeetingEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaMeetingEventPublisher.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public KafkaMeetingEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	public void publish(String topic, UUID meetingUuid, String eventType, Instant occurredAt, Object payload) {
		if (topic == null || topic.isBlank()) {
			throw new IllegalArgumentException("Kafka topic is required for " + eventType);
		}
		EventEnvelope<Object> envelope = new EventEnvelope<>(
				UUID.randomUUID().toString(),
				eventType,
				occurredAt,
				meetingUuid.toString(),
				EventEnvelope.CURRENT_VERSION,
				payload
		);
		String body;
		try {
			body = objectMapper.writeValueAsString(envelope);
		}
		catch (JsonProcessingException exception) {
			log.error("Kafka meeting event serialize failed: eventType={} meetingUuid={}", eventType, meetingUuid);
			return;
		}
		CompletableFuture.runAsync(() -> send(topic, meetingUuid, eventType, body));
	}

	private void send(String topic, UUID meetingUuid, String eventType, String body) {
		try {
			kafkaTemplate.send(topic, meetingUuid.toString(), body).whenComplete((result, exception) -> {
				if (exception != null) {
					log.error(
							"Kafka meeting event send failed (retry/DLT on broker-consumer): eventType={} topic={} meetingUuid={}",
							eventType,
							topic,
							meetingUuid
					);
					return;
				}
				log.info("Kafka meeting event sent: eventType={} topic={} meetingUuid={}", eventType, topic, meetingUuid);
			});
		}
		catch (RuntimeException exception) {
			log.error("Kafka meeting event send threw: eventType={} topic={} meetingUuid={}", eventType, topic, meetingUuid);
		}
	}
}
