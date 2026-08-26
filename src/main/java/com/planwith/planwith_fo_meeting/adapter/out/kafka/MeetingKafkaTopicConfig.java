package com.planwith.planwith_fo_meeting.adapter.out.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import com.planwith.planwith_fo_meeting.config.MeetingKafkaProperties;

@Configuration
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class MeetingKafkaTopicConfig {

	@Bean
	NewTopic meetingCreatedTopic(MeetingKafkaProperties properties) {
		return topic(properties.createdTopic());
	}

	@Bean
	NewTopic meetingCompletedTopic(MeetingKafkaProperties properties) {
		return topic(properties.completedTopic());
	}

	@Bean
	NewTopic meetingDisbandedTopic(MeetingKafkaProperties properties) {
		return topic(properties.disbandedTopic());
	}

	@Bean
	NewTopic meetingParticipationChangedTopic(MeetingKafkaProperties properties) {
		return topic(properties.participationChangedTopic());
	}

	@Bean
	NewTopic meetingUpdatedTopic(MeetingKafkaProperties properties) {
		return topic(properties.updatedTopic());
	}

	@Bean
	NewTopic meetingViceHostChangedTopic(MeetingKafkaProperties properties) {
		return topic(properties.viceHostChangedTopic());
	}

	private static NewTopic topic(String name) {
		return TopicBuilder.name(name).partitions(1).replicas(1).build();
	}
}
