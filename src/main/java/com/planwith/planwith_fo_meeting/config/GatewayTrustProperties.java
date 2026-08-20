package com.planwith.planwith_fo_meeting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.gateway")
public record GatewayTrustProperties(
		String internalToken,
		boolean trustCheckEnabled
) {
}
