package com.planwith.planwith_fo_meeting.adapter.out.kafka.dto;

import java.time.Instant;

public record EventEnvelope<T>(
		String eventId,
		String eventType,
		Instant occurredAt,
		String aggregateId,
		int version,
		T payload
) {

	public static final int CURRENT_VERSION = 1;
}
