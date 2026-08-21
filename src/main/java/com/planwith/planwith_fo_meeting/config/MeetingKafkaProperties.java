package com.planwith.planwith_fo_meeting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record MeetingKafkaProperties(
		boolean enabled,
		String createdTopic,
		String completedTopic,
		String disbandedTopic,
		String participationChangedTopic,
		String updatedTopic,
		String viceHostChangedTopic
) {
}
