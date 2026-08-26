package com.planwith.planwith_fo_meeting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.schedule")
public record ScheduleClientProperties(String baseUrl) {
}
