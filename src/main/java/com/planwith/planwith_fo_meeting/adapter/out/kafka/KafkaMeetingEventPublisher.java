package com.planwith.planwith_fo_meeting.adapter.out.kafka;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_meeting.adapter.out.kafka.dto.EventEnvelope;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaMeetingEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaMeetingEventPublisher.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final Environment environment;

	public KafkaMeetingEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
		this(kafkaTemplate, objectMapper, null);
	}

	@Autowired
	public KafkaMeetingEventPublisher(
			KafkaTemplate<String, String> kafkaTemplate,
			ObjectMapper objectMapper,
			Environment environment
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.environment = environment;
	}

	@PostConstruct
	void logProducerReady() {
		String bootstrap = environment == null ? null : environment.getProperty("spring.kafka.bootstrap-servers");
		String maxBlockMs = environment == null
				? null
				: environment.getProperty("spring.kafka.producer.properties.max.block.ms");
		log.info(
				"Kafka meeting producer enabled bootstrap={} maxBlockMs={}",
				bootstrap,
				maxBlockMs
		);
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
			log.error(
					"Kafka meeting event serialize failed: eventType={} meetingUuid={}",
					eventType,
					meetingUuid,
					exception
			);
			return;
		}
		enqueue(topic, meetingUuid, eventType, body);
	}

	private void enqueue(String topic, UUID meetingUuid, String eventType, String body) {
		log.info("Kafka meeting event publishing: eventType={} topic={} meetingUuid={}", eventType, topic, meetingUuid);
		Runnable send = () -> send(topic, meetingUuid, eventType, body);
		if (TransactionSynchronizationManager.isSynchronizationActive()
				&& TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					send.run();
				}
			});
			return;
		}
		send.run();
	}

	private void send(String topic, UUID meetingUuid, String eventType, String body) {
		try {
			kafkaTemplate.send(topic, meetingUuid.toString(), body).whenComplete((result, exception) -> {
				if (exception != null) {
					log.error(
							"Kafka meeting event send failed: eventType={} topic={} meetingUuid={}",
							eventType,
							topic,
							meetingUuid,
							exception
					);
					return;
				}
				log.info("Kafka meeting event sent: eventType={} topic={} meetingUuid={}", eventType, topic, meetingUuid);
			});
		}
		catch (RuntimeException exception) {
			log.error(
					"Kafka meeting event send threw: eventType={} topic={} meetingUuid={}",
					eventType,
					topic,
					meetingUuid,
					exception
			);
		}
	}
}
