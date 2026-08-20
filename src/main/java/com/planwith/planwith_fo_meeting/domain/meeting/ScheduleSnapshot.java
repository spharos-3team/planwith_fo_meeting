package com.planwith.planwith_fo_meeting.domain.meeting;

import java.time.Instant;
import java.util.UUID;

public record ScheduleSnapshot(
		UUID scheduleUuid,
		String destination,
		Instant startAt,
		Instant endAt,
		String cost,
		String transport
) {
}
