package com.planwith.planwith_fo_meeting.domain.meeting;

import java.time.LocalDate;
import java.util.UUID;

public record ScheduleSnapshot(
		UUID scheduleUuid,
		String destination,
		LocalDate startDate,
		LocalDate endDate
) {
}
